package dev.jcri.mdde.registry.shared.configuration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Generic database connection config, should fit most of the databases that can be accessed through network.
 */
@JsonPropertyOrder({
        DBNetworkNodesConfiguration.ID_FIELD,
        DBNetworkNodesConfiguration.HOST_FIELD,
        DBNetworkNodesConfiguration.PORT_FIELD,
        DBNetworkNodesConfiguration.USERNAME_FIELD,
        DBNetworkNodesConfiguration.PASSWORD_FIELD,
        DBNetworkNodesConfiguration.DIRECTORY_FIELD,
        DBNetworkNodesConfiguration.DEFAULT_NODE_FIELD
})
public class DBNetworkNodesConfiguration {
    public static final String ID_FIELD = "id";
    public static final String PORT_FIELD = "port";
    public static final String PASSWORD_FIELD = "password";
    public static final String USERNAME_FIELD = "username";
    public static final String HOST_FIELD = "host";
    public static final String DIRECTORY_FIELD = "dir";
    public static final String DEFAULT_NODE_FIELD = "default";

    /**
     * MDDE ID for the node (must be unique)
     */
    private String nodeId;
    /**
     * Network host (IP, Domain, netBios name)
     */
    private String host = null;
    /**
     * Database port
     */
    private Integer port = 6379;
    /**
     * If the used database requires a username alongside the password
     */
    private String username = null;
    /**
     * Database password if needed
     */
    private char[] password = null;
    /**
     * Database directory (ex. specific database within an SQL Server instance) if needed
     */
    private String directory = null;
    /**
     * Default nodes are pre-populated upon registry creation
     */
    private Boolean defaultNode = true;

    /**
     * Get ID of the node.
     * @return Database node assigned Id within the MDDE registry.
     */
    @JsonGetter(ID_FIELD)
    public String getNodeId(){
        return nodeId;
    }

    /**
     * Set ID of the node. The ID must be unique.
     * @param nId Database node assigned Id within the MDDE registry.
     */
    @JsonSetter(ID_FIELD)
    public void setNodeId(String nId){
        this.nodeId = nId;
    }

    /**
     * Get HOST name or the IP address of the database node.
     * @return Database node host.
     */
    @JsonGetter(HOST_FIELD)
    public String getHost() {
        return host;
    }

    /**
     * Set HOST name or the IP address of the database node.
     * @param redisHost Database node host.
     */
    @JsonSetter(HOST_FIELD)
    public void setHost(String redisHost) {
        this.host = redisHost;
    }

    /**
     * Get port of the database node.
     * @return Database node port.
     */
    @JsonGetter(PORT_FIELD)
    public Integer getPort() {
        return port;
    }

    /**
     * Set port of the database node.
     * @param redisPort Database node port.
     */
    @JsonSetter(PORT_FIELD)
    public void setPort(Integer redisPort) {
        this.port = redisPort;
    }

    /**
     * Get the username needed for the database connection
     * @return Username
     */
    @JsonGetter(USERNAME_FIELD)
    public String getUsername() {
        return username;
    }

    /**
     * Set the username if needed for the database connection
     * @param username Username
     */
    @JsonSetter(USERNAME_FIELD)
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get password of the database node.
     * @return Database node password.
     */
    @JsonGetter(PASSWORD_FIELD)
    public char[] getPassword() {
        return password;
    }

    /**
     * Set password of the database node if required.
     * @param redisPassword Database node password.
     */
    @JsonSetter(PASSWORD_FIELD)
    public void setPassword(char[] redisPassword) {
        this.password = redisPassword;
    }

    /**
     * Get the directory in the database.
     * @return Name of the DB directory
     */
    @JsonSetter(DIRECTORY_FIELD)
    public String getDirectory() {
        return directory;
    }

    /**
     * Set the directory in the database if required.
     * @param directory Name of the DB directory
     */
    @JsonSetter(DIRECTORY_FIELD)
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Default node flag
     * @return
     * True - the node should be populated in the Registry store upon its initialization.
     * False - node is configured but not used initially (future scalability testing).
     */
    @JsonSetter(DEFAULT_NODE_FIELD)
    public Boolean getDefaultNode() {
        return defaultNode;
    }

    /**
     * Default node flag
     * @param defaultNode
     * True - the node should be populated in the Registry store upon its initialization.
     * False - node is configured but not used initially (future scalability testing).
     */
    @JsonSetter(DEFAULT_NODE_FIELD)
    public void setDefaultNode(Boolean defaultNode) {
        this.defaultNode = defaultNode;
    }
}
