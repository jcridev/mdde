package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.server.CommandProcessor;

public class Main {

    public static void main(String args[]){

        var listener = new Listener();
        System.out.println(EWriteCommand.APPEND_TO_FRAGMENT.getCommand());
    }
}
