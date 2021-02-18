package edis.krsmanovic.genapi.config;

import com.google.common.collect.Lists;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class HazelcastConfig {
    @Value("${hazelcast.cluster.addresses}")
    private String hazelcastAddresses;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance(createConfig());
    }

    @Bean
    public IMap<Integer, Integer> nextResponseMap() {
        return hazelcastInstance().getMap("nextResponseMap");
    }

    private Config createConfig() {
        List<String> members = getIpAddresses();

        NetworkConfig network = new NetworkConfig();
        JoinConfig join = network.getJoin();
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(members);
        join.getMulticastConfig().setEnabled(true);

        Config config = new Config();
        config.setNetworkConfig(network);
        config.setProperty("hazelcast.initial.min.cluster.size","1");
        return config;
    }

    private List<String> getIpAddresses() {
        if (StringUtil.isNullOrEmpty(hazelcastAddresses)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(hazelcastAddresses.split(";"));
    }
}
