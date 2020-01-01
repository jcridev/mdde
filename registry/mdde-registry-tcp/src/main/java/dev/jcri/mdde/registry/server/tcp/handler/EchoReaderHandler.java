package dev.jcri.mdde.registry.server.tcp.handler;

/**
 * Echo the received payload back to the server.
 * For the server testing purposes.
 */
public class EchoReaderHandler extends MddeCommandReaderHandler {
    @Override
    protected String processCommand(String command) {
        logger.trace("Echo back length: {}", command.length());
        return command;
    }
}
