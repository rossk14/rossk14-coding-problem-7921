package com.rosskerr.fireholipexclusion.api.management;

import java.util.ArrayList;
import java.util.List;

/**
 * An ipset or netset file
 * Contains a list of blocked addresses to exclude 
 * and the name of the source list
 */
public class BlockedIpCidrSet {
    private String setName;
    private List<BlockedIpCidr> blockedAddresses = new ArrayList<>();

    public BlockedIpCidrSet withSetName(String value) {
        this.setName = value;
        return this;
    }
    public BlockedIpCidrSet addBlockedAddress(BlockedIpCidr value) {
        this.blockedAddresses.add(value);
        return this;
    }

    public String getSetName() {
        return setName;
    }
    public void setSetName(String value) {
        this.setName = value;
    }
    public List<BlockedIpCidr> getBlockedAddresses() {
        return blockedAddresses;
    }
    public void setBlockedAddress(List<BlockedIpCidr> blockedAddresses) {
        this.blockedAddresses = blockedAddresses;
    }

    
}
