/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.manual.maps;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MatchedIPPrefix {
    private final int prefixLen;
    private final InetAddress baseAddress;

    public MatchedIPPrefix(String ipAddress) {
        int _prefixLen = -1;
        if (ipAddress.indexOf('/') > 0) {
            String[] baseAndLen = ipAddress.split("/");
            ipAddress = baseAndLen[0];
            _prefixLen = Integer.parseInt(baseAndLen[1]);
        }
        try {
            baseAddress = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to parse address " + ipAddress, e);
        }
        if (_prefixLen < 0) {
            _prefixLen = baseAddress.getAddress().length * 8;
        }
        prefixLen = _prefixLen;
        assert (baseAddress.getAddress().length * 8 >= prefixLen):
                String.format("Prefix length %d is too long for the base IP address %s", prefixLen, baseAddress);
    }

    /**
     * Return if the current IP prefix can cover the taken specific IP prefix.
     *
     * @param specificPrefix a specific IP prefix.
     * @return if the taken specific IP prefix is covered.
     */
    public boolean match(@Nonnull MatchedIPPrefix specificPrefix) {
        if (!baseAddress.getClass().equals(specificPrefix.baseAddress.getClass())) {
            return false;
        }
        if (prefixLen > specificPrefix.prefixLen) {
            return false;
        }
        byte[] baseAddr = baseAddress.getAddress();
        byte[] specAddr = specificPrefix.baseAddress.getAddress();

        int nMaskedFullBytes = prefixLen / 8;
        byte lastByteMask = (byte) (0xFF00 >> (prefixLen & 0x07));
        for (int i = 0; i < nMaskedFullBytes; i++) {
            if (baseAddr[i] != specAddr[i]) {
                return false;
            }
        }
        if (lastByteMask != 0) {
            return (baseAddr[nMaskedFullBytes] & lastByteMask) == (specAddr[nMaskedFullBytes] & lastByteMask);
        }
        return true;
    }
}
