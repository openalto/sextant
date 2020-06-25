/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AltoTopologyTest {
    private AltoTopology topology;

    @Before
    public void loadTopology() {
        topology = new AltoTopology();

        topology.addNode(0L, 673720322L);
        topology.addNode(0L, 168430089L);
        topology.addNode(0L, 168430088L);
        topology.addNode(1L, 168430085L);
        topology.addNode(1L, 168430086L);
        topology.addNode(0L, 168430087L);
        topology.addNode(0L, 168430084L);
        topology.addNode(1L, 168430084L);
        topology.addNode(1L, 168430081L);
        topology.addNode(0L, 168430082L);
        topology.addNode(1L, 168430082L);
        topology.addNode(0L, 168430083L);

        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKAgEDAAQdHR0CAQQABB0dHQE=", 0L, 168430089L, 168430082L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEDAAQtLS0CAQQABC0tLQE=", 1L, 168430085L, 168430084L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCAEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEDAARZWVkBAQQABFlZWQI=", 0L, 168430088L, 168430089L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEDAAQODg4BAQQABA4ODgI=", 1L, 168430081L, 168430084L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKBwEDAARPT08CAQQABE9PTwE=", 0L, 168430089L, 168430087L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKAwEDAAQnJycCAQQABCcnJwE=", 0L, 168430089L, 168430083L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAEKCgoAgEDAARaWloBAQQABFpaWgI=", 0L, 168430089L, 673720322L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAEKCgoAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKBAEDAAQoKCgCAQQABCgoKAE=", 0L, 673720322L, 168430084L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKAwEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEDAAQnJycBAQQABCcnJwI=", 0L, 168430083L, 168430089L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAEKCgoAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEDAARaWloCAQQABFpaWgE=", 0L, 673720322L, 168430089L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCAEDAARZWVkCAQQABFlZWQE=", 0L, 168430089L, 168430088L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEDAAQaGhoBAQQABBoaGgI=", 1L, 168430086L, 168430082L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKBAEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAEKCgoAgEDAAQoKCgBAQQABCgoKAI=", 0L, 168430084L, 673720322L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEDAAQuLi4CAQQABC4uLgE=", 1L, 168430086L, 168430084L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBQEDAAQtLS0BAQQABC0tLQI=", 1L, 168430084L, 168430085L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKBwEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEDAARPT08BAQQABE9PTwI=", 0L, 168430087L, 168430089L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBgEDAAQaGhoCAQQABBoaGgE=", 1L, 168430082L, 168430086L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBQEDAAQZGRkBAQQABBkZGQI=", 1L, 168430082L, 168430085L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEDAAQZGRkCAQQABBkZGQE=", 1L, 168430085L, 168430082L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAQEDAAQODg4CAQQABA4ODgE=", 1L, 168430084L, 168430081L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBAEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKBgEDAAQuLi4BAQQABC4uLgI=", 1L, 168430084L, 168430086L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAQEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEDAAQMDAwBAQQABAwMDAI=", 1L, 168430081L, 168430082L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAACAwAECgoKCQEDAAQdHR0BAQQABB0dHQI=", 0L, 168430082L, 168430089L);
        topology.addLink("AAIAYQMAAAAAAAAABAEAACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAgEBACACAAAEAAAAZAIBAAQAAAAAAgIABAAAAAECAwAECgoKAQEDAAQMDAwCAQQABAwMDAE=", 1L, 168430082L, 168430081L);

        topology.addIntraPrefix("46.46.46.0/30", 1L, 168430084L);
        topology.addIntraPrefix("79.79.79.0/30", 0L, 168430087L);
        topology.addIntraPrefix("6.6.6.0/24", 1L, 168430086L);
        topology.addIntraPrefix("25.25.25.0/30", 1L, 168430082L);
        topology.addIntraPrefix("40.40.40.0/30", 0L, 673720322L);
        topology.addIntraPrefix("26.26.26.0/30", 1L, 168430086L);
        topology.addIntraPrefix("4.4.4.0/24", 1L, 168430084L);
        topology.addIntraPrefix("10.10.10.7/32", 0L, 168430087L);
        topology.addIntraPrefix("89.89.89.0/30", 0L, 168430089L);
        topology.addIntraPrefix("26.26.26.0/30", 1L, 168430082L);
        topology.addIntraPrefix("46.46.46.0/30", 1L, 168430086L);
        topology.addIntraPrefix("5.5.5.0/24", 1L, 168430085L);
        topology.addIntraPrefix("39.39.39.0/30", 0L, 168430083L);
        topology.addIntraPrefix("14.14.14.0/30", 1L, 168430081L);
        topology.addIntraPrefix("25.25.25.0/30", 1L, 168430085L);
        topology.addIntraPrefix("45.45.45.0/30", 1L, 168430084L);
        topology.addIntraPrefix("10.10.10.4/32", 1L, 168430084L);
        topology.addIntraPrefix("45.45.45.0/30", 1L, 168430085L);
        topology.addIntraPrefix("40.40.40.0/30", 0L, 168430084L);
        topology.addIntraPrefix("90.90.90.0/30", 0L, 673720322L);
        topology.addIntraPrefix("10.10.10.3/32", 0L, 168430083L);
        topology.addIntraPrefix("29.29.29.0/30", 0L, 168430082L);
        topology.addIntraPrefix("10.10.10.6/32", 1L, 168430086L);
        topology.addIntraPrefix("12.12.12.0/30", 1L, 168430082L);
        topology.addIntraPrefix("89.89.89.0/30", 0L, 168430088L);
        topology.addIntraPrefix("1.1.1.0/24", 1L, 168430081L);
        topology.addIntraPrefix("29.29.29.0/30", 0L, 168430089L);
        topology.addIntraPrefix("10.10.10.2/32", 1L, 168430082L);
        topology.addIntraPrefix("14.14.14.0/30", 1L, 168430084L);
        topology.addIntraPrefix("10.10.10.5/32", 1L, 168430085L);
        topology.addIntraPrefix("39.39.39.0/30", 0L, 168430089L);
        topology.addIntraPrefix("10.10.10.1/32", 1L, 168430081L);
        topology.addIntraPrefix("90.90.90.0/30", 0L, 168430089L);
        topology.addIntraPrefix("12.12.12.0/30", 1L, 168430081L);
        topology.addIntraPrefix("79.79.79.0/30", 0L, 168430089L);

        topology.addInterPrefix("10.10.10.1/32", 0L, 168430084L, 2L);
        topology.addInterPrefix("25.25.25.0/30", 0L, 168430082L, 1L);
        topology.addInterPrefix("8.8.8.0/24", 1L, 168430084L, 4L);
        topology.addInterPrefix("10.10.10.2/32", 0L, 168430084L, 3L);
        topology.addInterPrefix("10.10.10.4/32", 0L, 168430084L, 1L);
        topology.addInterPrefix("14.14.14.0/30", 0L, 168430084L, 1L);
        topology.addInterPrefix("14.14.14.0/30", 0L, 168430082L, 2L);
        topology.addInterPrefix("10.10.10.3/32", 1L, 168430084L, 4L);
        topology.addInterPrefix("79.79.79.0/30", 1L, 168430084L, 3L);
        topology.addInterPrefix("8.8.8.0/24", 0L, 168430088L, 1L);
        topology.addInterPrefix("29.29.29.0/30", 1L, 168430082L, 1L);
        topology.addInterPrefix("8.8.8.0/24", 1L, 168430082L, 3L);
        topology.addInterPrefix("26.26.26.0/30", 0L, 168430084L, 2L);
        topology.addInterPrefix("29.29.29.0/30", 1L, 168430084L, 3L);
        topology.addInterPrefix("90.90.90.0/30", 1L, 168430084L, 2L);
        topology.addInterPrefix("40.40.40.0/30", 1L, 168430082L, 3L);
        topology.addInterPrefix("89.89.89.0/30", 1L, 168430084L, 3L);
        topology.addInterPrefix("12.12.12.0/30", 0L, 168430082L, 1L);
        topology.addInterPrefix("79.79.79.0/30", 1L, 168430082L, 2L);
        topology.addInterPrefix("10.10.10.3/32", 1L, 168430082L, 3L);
        topology.addInterPrefix("45.45.45.0/30", 0L, 168430084L, 1L);
        topology.addInterPrefix("40.40.40.0/30", 1L, 168430084L, 1L);
        topology.addInterPrefix("1.1.1.0/24", 0L, 168430082L, 2L);
        topology.addInterPrefix("39.39.39.0/30", 1L, 168430084L, 3L);
        topology.addInterPrefix("81.81.81.0/30", 1L, 168430082L, 3L);
        topology.addInterPrefix("10.10.10.11/32", 1L, 168430082L, 4L);
        topology.addInterPrefix("11.11.11.0/24", 1L, 168430082L, 4L);
        topology.addInterPrefix("46.46.46.0/30", 0L, 168430084L, 1L);
        topology.addInterPrefix("6.6.6.0/24", 0L, 168430082L, 2L);
        topology.addInterPrefix("45.45.45.0/30", 0L, 168430082L, 2L);
        topology.addInterPrefix("89.89.89.0/30", 1L, 168430082L, 2L);
        topology.addInterPrefix("26.26.26.0/30", 0L, 168430082L, 1L);
        topology.addInterPrefix("10.10.10.7/32", 1L, 168430082L, 3L);
        topology.addInterPrefix("10.10.10.8/32", 0L, 168430088L, 1L);
        topology.addInterPrefix("10.10.10.8/32", 1L, 168430082L, 3L);
        topology.addInterPrefix("10.10.10.4/32", 0L, 168430082L, 3L);
        topology.addInterPrefix("46.46.46.0/30", 0L, 168430082L, 2L);
        topology.addInterPrefix("25.25.25.0/30", 0L, 168430084L, 2L);
        topology.addInterPrefix("11.11.11.0/24", 1L, 168430084L, 5L);
        topology.addInterPrefix("5.5.5.0/24", 0L, 168430082L, 2L);
        topology.addInterPrefix("10.10.10.8/32", 1L, 168430084L, 4L);
        topology.addInterPrefix("4.4.4.0/24", 0L, 168430084L, 1L);
        topology.addInterPrefix("10.10.10.6/32", 0L, 168430082L, 2L);
        topology.addInterPrefix("10.10.10.11/32", 1L, 168430084L, 5L);
        topology.addInterPrefix("81.81.81.0/30", 1L, 168430084L, 4L);
        topology.addInterPrefix("5.5.5.0/24", 0L, 168430084L, 2L);
        topology.addInterPrefix("10.10.10.11/32", 0L, 168430088L, 2L);
        topology.addInterPrefix("10.10.10.5/32", 0L, 168430082L, 2L);
        topology.addInterPrefix("11.11.11.0/24", 0L, 168430088L, 2L);
        topology.addInterPrefix("10.10.10.6/32", 0L, 168430084L, 2L);
        topology.addInterPrefix("90.90.90.0/30", 1L, 168430082L, 2L);
        topology.addInterPrefix("10.10.10.5/32", 0L, 168430084L, 2L);
        topology.addInterPrefix("6.6.6.0/24", 0L, 168430084L, 2L);
        topology.addInterPrefix("81.81.81.0/30", 0L, 168430088L, 1L);
        topology.addInterPrefix("12.12.12.0/30", 0L, 168430084L, 2L);
        topology.addInterPrefix("10.10.10.2/32", 0L, 168430082L, 1L);
        topology.addInterPrefix("10.10.10.7/32", 1L, 168430084L, 4L);
        topology.addInterPrefix("1.1.1.0/24", 0L, 168430084L, 2L);
        topology.addInterPrefix("39.39.39.0/30", 1L, 168430082L, 2L);
        topology.addInterPrefix("4.4.4.0/24", 0L, 168430082L, 3L);
        topology.addInterPrefix("10.10.10.1/32", 0L, 168430082L, 2L);

        topology.buildDistance();
    }

    @Test
    public void getDistance() {
        int dist1 = topology.getDistance("4.4.4.0/24", "5.5.5.0/24");
        assertEquals(2, dist1);
        int dist2 = topology.getDistance("5.5.5.0/24", "1.1.1.0/24");
        assertEquals(3, dist2);
        System.out.println(topology.getDistance(Arrays.asList("1.1.1.0/24"), Arrays.asList("4.4.4.0/24")));
        System.out.println(topology.getDistance(Arrays.asList("1.1.1.0/24"), Arrays.asList("5.5.5.0/24")));
        System.out.println(topology.getDistance(Arrays.asList("1.1.1.0/24"), Arrays.asList("6.6.6.0/24")));
        System.out.println(topology.getDistance(Arrays.asList("1.1.1.0/24"), Arrays.asList("8.8.8.0/24")));
        System.out.println(topology.getDistance(Arrays.asList("1.1.1.0/24"), Arrays.asList("11.11.11.0/24")));
    }
}