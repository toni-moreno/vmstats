package org.timconrad.fakecarbon;

/*
 * Copyright 2012 Tim Conrad - tim@timconrad.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class FakeCarbonHandler extends SimpleChannelUpstreamHandler {

    private final Hashtable<String, String> appConfig;
    private static final Logger logger = LoggerFactory.getLogger(FakeCarbonHandler.class);
    private int count = 1;

    public FakeCarbonHandler(Hashtable<String, String> appConfig) {
       this.appConfig = appConfig;
    }

    public boolean checkStat(String stat) {
        boolean results = false;
        String[] tokens = stat.split("[ ]+");
        try {
            Float.parseFloat(tokens[1]);
            results = true;
        }catch(NumberFormatException e){
            results = false;
            logger.debug("string: " + stat + " Error in stat field.");
        }
        try {
            Float.parseFloat(tokens[2]);
            results = true;
        }catch(NumberFormatException e) {
            results = false;
            logger.debug("string: " + stat + " Error in stat field.");
        }
        return results;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if(e instanceof ChannelStateEvent){
            System.out.println("ChannelStateEvent: " + e.toString());
        }
        super.handleUpstream(ctx,e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // do nothing
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("Client has disconnected. Counter: " + count);
        // reset packet count to 0
        count = 0;

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        String request = (String) e.getMessage();
        boolean result = checkStat(request);
        String tag;
        if(result) {
            tag = "PASS";
        }else{
            tag = "FAIL";
        }
        if(appConfig.get("displayAll").contains("true")){
            logger.info("packet(" + count + "): " + request + " results: " + tag);
        }
        if(appConfig.get("displayBad").contains("true")){
            if (!result){
                logger.info("packet(" + count + "): " + request + " results: " + tag);
            }
        }
        count++;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        System.out.println("Unexpected exception from downstream: " + e.getCause());
        e.getChannel().close();
    }
}
