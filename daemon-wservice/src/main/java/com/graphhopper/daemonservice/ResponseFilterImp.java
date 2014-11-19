/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.daemonservice;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 *
 * @author elgammaa
 */
public class ResponseFilterImp implements ContainerResponseFilter
{
    @Override
    public ContainerResponse filter( final ContainerRequest request, final ContainerResponse response )
    {
        final ResponseBuilder resp = Response.fromResponse(response.getResponse());
        resp.header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        final String reqHead = request.getHeaderValue("Access-Control-Request-Headers");
        if (null != reqHead && !reqHead.equals(null))
        {
            resp.header("Access-Control-Allow-Headers", reqHead);
        }
        response.setResponse(resp.build());
        return response;

    }

}
