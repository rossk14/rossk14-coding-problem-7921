package com.rosskerr.fireholipexclusion.api.management;

/**
 * A record in an ipset or netset file
 * Contains a single ip to exclude or a CIDR range of 
 * addresses to exclude
 */
public class BlockedIpCidr {
    private String blockedAddress;

    public BlockedIpCidr withBlockedAddress(String value) {
        this.blockedAddress = value;
        return this;
    }

    public String getBlockedAddress() {
        return blockedAddress;
    }
    public void setBlockedAddress(String blockedAddress) {
        this.blockedAddress = blockedAddress;
    }

    
}
