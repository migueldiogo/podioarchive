package com.podio;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

public class RateLimitFilter extends ClientFilter {

    final static double RATE_LIMIT_THRESHOLD = 20;
    private long restTimeMillis;
    private boolean calls1000Blocked = false;
    private boolean calls5000Blocked = false;

    @SuppressWarnings("unchecked")
    @Override
    public ClientResponse handle(ClientRequest cr) {
        ClientResponse response = getNext().handle(cr);
        MultivaluedMap<String, String> headers = response.getHeaders();
        if (headers != null) {
            List<String> limitHeader = headers.get("X-Rate-Limit-Limit");
            if (limitHeader != null && limitHeader.size() == 1) {
                try {
                    RateLimits.setLimit(Integer.parseInt(limitHeader.get(0)));
                } catch (NumberFormatException nfe) {
                    RateLimits.setLimit(null);
                }
            } else {
                RateLimits.setLimit(null);
            }
            List<String> remainingHeader = response.getHeaders().get("X-Rate-Limit-Remaining");
            if (remainingHeader != null && remainingHeader.size() == 1) {
                try {
                    RateLimits.setRemaining(Integer.parseInt(remainingHeader.get(0)));
                } catch (NumberFormatException nfe) {
                    RateLimits.setRemaining(null);
                }
            } else {
                RateLimits.setRemaining(null);
            }
        } else {
            RateLimits.setLimit(null);
            RateLimits.setRemaining(null);
        }

        System.out.println("[X-Rate-Limit-Limit] " + RateLimits.getLimit() + "; [X-Rate-Limit-Remaining] " + RateLimits.getRemaining());

        try {
            rateLimitChecker();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void rateLimitChecker() throws InterruptedException {
        if (RateLimits.getResetTime() == null)
            RateLimits.setResetTime(System.currentTimeMillis());

        if (RateLimits.getLimit() != null && RateLimits.getRemaining() != null) {
            checkConditions();

            if (calls1000Blocked && calls5000Blocked)
                sleep();
            else if (RateLimits.getLimit() == 1000 && calls1000Blocked)
                sleep();
            else if (RateLimits.getLimit() == 5000 && calls5000Blocked)
                sleep();
        }
    }

    private boolean checkConditions() {
        //int thresholdMinimum = (int)(RateLimits.getLimit() * (RATE_LIMIT_THRESHOLD/100));
        boolean result = true;

        if(RateLimits.getLimit() == 1000 && RateLimits.getRemaining() <= RATE_LIMIT_THRESHOLD) {
            calls1000Blocked = true;
            result = false;
        }  else if (RateLimits.getLimit() == 5000 && RateLimits.getRemaining() <= RATE_LIMIT_THRESHOLD) {
            calls5000Blocked = true;
            result = false;
        }

        return result;
    }

    private void sleep() {
        restTimeMillis = 62 * 60 * 1000 - (System.currentTimeMillis() - RateLimits.getResetTime());
        System.out.println("[CORE] Sleeping for " + restTimeMillis/1000/60 + " minutes...");

        try {
            Thread.sleep(2);

            calls1000Blocked = false;
            calls5000Blocked = false;
            RateLimits.setResetTime(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
