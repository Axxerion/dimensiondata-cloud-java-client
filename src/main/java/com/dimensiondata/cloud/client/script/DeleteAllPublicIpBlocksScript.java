package com.dimensiondata.cloud.client.script;

import com.dimensiondata.cloud.client.*;
import com.dimensiondata.cloud.client.model.PublicIpBlockType;
import com.dimensiondata.cloud.client.model.PublicIpBlocks;

import java.util.List;

import static com.dimensiondata.cloud.client.script.Script.*;

public class DeleteAllPublicIpBlocksScript implements NetworkDomainScript
{
    @Override
    public void execute(Cloud cloud, String networkDomainId)
    {
        Filter filter = new Filter(new Param(IpAddressService.PARAMETER_NETWORKDOMAIN_ID, networkDomainId));
        PublicIpBlocks blocks = cloud.ipAddress().listPublicIpBlocks(PAGE_SIZE, 1, OrderBy.EMPTY, filter);
        println("Public Ip Blocks to remove: " + blocks.getTotalCount());
        while (blocks.getTotalCount() > 0)
        {
            deleteBlocks(cloud, blocks.getPublicIpBlock());
            blocks = cloud.ipAddress().listPublicIpBlocks(PAGE_SIZE, 1, OrderBy.EMPTY, filter);
        }
    }

    private static void deleteBlocks(Cloud cloud, List<PublicIpBlockType> blocks)
    {
        // check all items are in a NORMAL state
        for (PublicIpBlockType block : blocks)
        {
            if (!block.getState().equals(NORMAL_STATE))
            {
                throw new RuntimeException("Block not in NORMAL state: " + block.getId());
            }
        }

        for (PublicIpBlockType block : blocks)
        {
            cloud.ipAddress().removePublicIpBlock(block.getId());
            // TODO enable again when CCD-7503 is fixed
            // awaitUntil("Removing Public Ip Block " + block.getId(), new CallableDeletedState(id -> cloud.ipAddress().getPublicIpBlock(id).getState(), "block", block.getId()));
        }
    }
}
