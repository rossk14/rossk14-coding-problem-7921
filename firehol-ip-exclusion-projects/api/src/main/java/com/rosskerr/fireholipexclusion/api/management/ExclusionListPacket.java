package com.rosskerr.fireholipexclusion.api.management;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of netset/ipset files
 */
public class ExclusionListPacket {
    private List<BlockedIpCidrSet> exclusionLists = new ArrayList<>();

    public ExclusionListPacket addExclusionList(BlockedIpCidrSet value) {
        this.exclusionLists.add(value);
        return this;
    }

    public List<BlockedIpCidrSet> getExclusionLists() {
        return exclusionLists;
    }
    public void setExclusionLists(List<BlockedIpCidrSet> exclusionLists) {
        this.exclusionLists = exclusionLists;
    }

    
}
