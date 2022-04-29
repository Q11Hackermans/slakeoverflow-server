package com.github.q11hackermans.slakeoverflow_server;

import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;

import java.io.IOException;

public class SlakeoverflowServer {

    private CMSServer connectionhandler;
    private DataIOManager dataIOManager;


    public SlakeoverflowServer() throws IOException {
        this.connectionhandler = new CMSServer(55555);
    }
    // HIER GEHTS LOS :) {"cmd":"tick","fields":[[0,1,0],[],[]]}  {"cmd":"auth","username":"vollidiot123"} {"cmd":"auth2","sizex":100,"sizey":100} {"cmd":"auth3"}

    public static void main(String[] args) throws IOException {
        new SlakeoverflowServer();
    }
}
