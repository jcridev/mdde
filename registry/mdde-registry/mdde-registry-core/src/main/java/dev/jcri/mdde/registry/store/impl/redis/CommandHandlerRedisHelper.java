package dev.jcri.mdde.registry.store.impl.redis;

/**
 * Singleton (lazy) class containing common methods that are shared within various Redis operations
 */
public class CommandHandlerRedisHelper {

    private CommandHandlerRedisHelper(){}

    private static class LazyHolder {
        static final CommandHandlerRedisHelper _instance = new CommandHandlerRedisHelper();
    }

    public static CommandHandlerRedisHelper sharedInstance(){
        return LazyHolder._instance;
    }

    /**
     * Generate redis Global key for the fragment meta value
     * @param fragmentId Fragment ID
     * @return
     */
    public String genGlobalFragmentMetaFieldName(final String fragmentId){
        return Constants.FRAGMENT_GLOBAL_META_PREFIX + fragmentId;
    }

    /**
     * Generate redis Exemplar key for Meta value
     * @param fragmentId Fragment ID
     * @param nodeId Node ID
     * @return
     */
    public String genExemplarFragmentMetaFieldName(final String fragmentId, final String nodeId){
        return String.format("%s%s/%s", Constants.FRAGMENT_EXEMPLAR_META_PREFIX, fragmentId, nodeId);
    }
}
