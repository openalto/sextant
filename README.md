# Sextant: Automated Network Information Collection, Abstraction & Exposure

## Installation

This plugin is still an on-going work. The official release has not included it yet.

To try it, you should download the latest [pre-released patch](https://github.com/openalto/odl-alto/releases) and follow the instruction to install it into a pre-installed OpenDaylight Oxygen-SR4 release.

## Prepare

Before trying this plugin, you should set up at least one BGP-LS session with your BGP speaker.

## Install Auto Map Features

To try out the alto-auto-maps plugin, you should install the following features in order:

``` bash
opendaylight-user@root>feature:install odl-alto-core
opendaylight-user@root>feature:install odl-alto-simpleird
opendaylight-user@root>feature:install odl-alto-manual-maps                                                                     
opendaylight-user@root>feature:install odl-alto-auto-maps
```

## Auto Map Configuration

You can use the Restconf API to create a new network map as follows:

``` http
PUT /restconf/config/alto-auto-maps:config-context/00000000-0000-0000-0000-000000000000/network-map-config/igp-bgp-networkmap HTTP/1.1
Host: localhost:8181
Content-Type: application/json

{
  "network-map-config": {
    "resource-id": "simple-bgp-networkmap",
    "bgp-params": {
      "bgp-rib": [
        {
          "rib-id": "alto-tcdn-ipv4",
          "bgp-ls": true
        }
      ]
    },
    "first-hop-cluster-algorithm": {
      "inspect-igp": false,
      "inspect-internal-link": false
    }
  }
}
```

In the configuration above, you should replace `simple-bgp-networkmap` with your own resource id, and replace `alto-tcdn-ipv4` with your own BGP-LS protocol instance id (you should have configured one in the [Prepare Stage](#prepare)).

Similarly, you can create a new cost map depending on the network map above:

``` http
PUT /restconf/config/alto-auto-maps:config-context/00000000-0000-0000-0000-000000000000/cost-map-config/igp-bgp-hopcount-costmap HTTP/1.1
Host: localhost:8181
Content-Type: application/json

{
  "cost-map-config": {
    "resource-id": "igp-bgp-hopcount-costmap",
    "dependent-network-map": "simple-bgp-networkmap",
    "bgp-params": {
      "alternative-bgp-rib": [
        {
          "rib-id": "alto-tcdn-ls",
          "bgp-ls": true
        }
      ]
    },
    "cost-type": [
      {
        "cost-mode": "numerical",
        "cost-metric": "hopcount"
      }
    ]
  }
}
```

Now you can try to access the default IRD of the ALTO server:

``` http
GET /alto/simpleird/default HTTP/1.1
Host: localhost:8181
Accept: application/alto-directory+json,application/alto-error+json

HTTP/1.1 200 OK
Content-Type: application/alto-directory+json

{
  "meta": {
    "cost-types": {}
  },
  "resources": {
    "igp-bgp-hopcount-costmap": {
      "media-type": "application/alto-costmap+json",
      "uri": "http://0:0:0:0:0:0:0:1:8181/alto/costmap/igp-bgp-hopcount-costmap",                                         
      "uses": [
        "igp-bgp-networkmap"
      ]
    },
    "igp-bgp-networkmap": {
      "media-type": "application/alto-networkmap+json",
      "uri": "http://0:0:0:0:0:0:0:1:8181/alto/networkmap/simple-bgp-networkmap"                                             
    }
  }
}
```

You will see that two ALTO information resources have been created. You can use their `uri` to access them now.

``` http
GET /alto/networkmap/simple-bgp-networkmap HTTP/1.1
Host: localhost:8181
Accepts: application/alto-networkmap+json

HTTP/1.1 200 OK
Content-Type: application/alto-networkmap+json

{
  "meta": {
    "vtag": {
      "resource-id": "simple-bgp-networkmap",
      "tag": "b5220086bfad4a6a9d231ae9b3370dcf"
    }
  },
  "network-map": {
    "PID0:0a0a0a01": {
      "ipv4": [
        "1.1.1.0/24"
      ]
    },
    "PID0:0a0a0a04": {
      "ipv4": [
        "4.4.4.0/24"
      ]
    },
    "PID0:0a0a0a05": {
      "ipv4": [
        "5.5.5.0/24"
      ]
    },
    "PID0:0a0a0a06": {
      "ipv4": [
        "6.6.6.0/24"
      ]
    },
    "PID0:0a0a0a08": {
      "ipv4": [
        "8.8.8.0/24"
      ]
    },
    "PID0:0a0a0a0b": {
      "ipv4": [
        "11.11.11.0/24"
      ]
    }
  }
}
```

``` http
GET /alto/costmap/igp-bgp-hopcount-costmap HTTP/1.1
Host: localhost:8181
Accepts: application/alto-costmap+json

HTTP/1.1 200 OK
Content-Type: application/alto-costmap+json

{
  "cost-map": {
    "PID0:0a0a0a01": {
      "PID0:0a0a0a04": "2",
      "PID0:0a0a0a05": "0",
      "PID0:0a0a0a06": "0",
      "PID0:0a0a0a08": "2",
      "PID0:0a0a0a0b": "3"
    },
    "PID0:0a0a0a04": {
      "PID0:0a0a0a01": "2",
      "PID0:0a0a0a05": "2",
      "PID0:0a0a0a06": "2",
      "PID0:0a0a0a08": "0",
      "PID0:0a0a0a0b": "2"
    },
    "PID0:0a0a0a05": {
      "PID0:0a0a0a01": "0",
      "PID0:0a0a0a04": "2",
      "PID0:0a0a0a06": "0",
      "PID0:0a0a0a08": "2",
      "PID0:0a0a0a0b": "3"
    },
    "PID0:0a0a0a06": {
      "PID0:0a0a0a01": "0",
      "PID0:0a0a0a04": "2",
      "PID0:0a0a0a05": "0",
      "PID0:0a0a0a08": "2",
      "PID0:0a0a0a0b": "3"
    },
    "PID0:0a0a0a08": {
      "PID0:0a0a0a01": "2",
      "PID0:0a0a0a04": "0",
      "PID0:0a0a0a05": "2",
      "PID0:0a0a0a06": "2",
      "PID0:0a0a0a0b": "2"
    },
    "PID0:0a0a0a0b": {
      "PID0:0a0a0a01": "3",
      "PID0:0a0a0a04": "2",
      "PID0:0a0a0a05": "3",
      "PID0:0a0a0a06": "3",
      "PID0:0a0a0a08": "2"
    }
  },
  "meta": {
    "cost-type": {
      "cost-metric": "hopcount",
      "cost-mode": "numerical"
    },
    "dependent-vtags": [
      {
        "resource-id": "simple-bgp-networkmap",
        "tag": "b5220086bfad4a6a9d231ae9b3370dcf"
      }
    ],
    "vtag": {
      "resource-id": "igp-bgp-hopcount-costmap",
      "tag": "e22a65b220c3454aa1ac743d31c00746"
    }
  }
}
```

## Missing Features

Again, this plugin is still on development. The ALTO information resource generated by this plugin is incomplete:

- [ ] The `capabilities` field in the IRD is missing;
- [ ] The filtered ALTO maps may have some issues.

And some ALTO mechanisms are still missing:

- [x] The first-hop network map MUST be generated based on the BGP-LS RIB right now. Therefore, some non-endpoint CIDRs will be included in the generated network map. We are working on generating a cleanup network map based on the BGP RIB.
- [ ] The cost map may not be updated correctly when the network map changes.

Also, we are also considering some practical concerns, which will be added in the future updates:

- [ ] access control (*e.g.*, authentication)
- [ ] more HTTP headers and status codes (*e.g.*, HTTP 304 Not Modified)

We will release the new patches to complete the missing features. If you have any other questions, please feel free to let us know.
