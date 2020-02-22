package dev.jcri.mdde.registry.benchmark.ycsb;

/**
 * Valid YCSB clients known by MDDE
 */
public enum EYCSBClients {
    MDDE_REDIS("mdde.redis");

    private final String _clientName;

    EYCSBClients(String clientName){
        if(clientName == null || clientName.isBlank()){
            throw new IllegalArgumentException("YCSB Client name can't be null or empty");
        }

        _clientName = clientName;
    }

    public String getClientName(){
        return _clientName;
    }

    /**
     *
     * @return Client name as it's specified in the target YCSB
     */
    @Override
    public String toString() {
        return _clientName;
    }

    /**
     * Get EYCSBClients value by client name
     * @param text Client name as it's specified in the target YCSB
     * @return EYCSBClients value if the client is known by the registry, null otherwise
     */
    public static EYCSBClients fromString(String text) {
        for (EYCSBClients value : EYCSBClients.values()) {
            if (value._clientName.equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }
}
