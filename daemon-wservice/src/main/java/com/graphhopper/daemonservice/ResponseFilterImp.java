/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.daemonservice;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 *
 * @author elgammaa
 */
public class ResponseFilterImp implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        System.err.print("pass par la");

/*        final ResponseBuilder resp = Response.fromResponse(containerResponseContext.get);
        resp.header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        final String reqHead = containerRequestContext.getHeaderString("Access-Control-Request-Headers");
        if (null != reqHead && !reqHead.equals(null)) {
            resp.header("Access-Control-Allow-Headers", reqHead);
        }
*/
    }

}
