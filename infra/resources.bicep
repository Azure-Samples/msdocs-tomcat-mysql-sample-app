param name string
param location string
param resourceToken string
param tags object
@secure()
param databasePassword string

var appName = '${name}-${resourceToken}'

// var databaseSubnetName = 'database-subnet'
// var webappSubnetName = 'webapp-subnet'

// Added for Azure Redis Cache
// var cacheServerName = '${appName}-redisCache'
// var cacheSubnetName = 'cache-subnet'
// var cachePrivateEndpointName = 'cache-privateEndpoint'
// var cachePvtEndpointDnsGroupName = 'cacheDnsGroup'

resource virtualNetwork 'Microsoft.Network/virtualNetworks@2023-04-01' = {
  location: location
  name: '${appName}Vnet'
  properties: {
    addressSpace: {
      addressPrefixes: ['10.0.0.0/16']
    }
    subnets: [
      {
        name: 'cache-subnet'
        properties: {
          addressPrefix: '10.0.0.0/24'
          privateEndpointNetworkPolicies: 'Disabled'
        }
      }
      {
        name: 'database-subnet'
        properties: {
          delegations: [
            {
              name: 'dlg-database'
              properties: {
                serviceName: 'Microsoft.DBforMySQL/flexibleServers'
              }
            }
          ]
          serviceEndpoints: []
          addressPrefix: '10.0.2.0/24'
        }
      }
      {
        name: 'webapp-subnet'
        properties: {
          delegations: [
            {
              name: 'dlg-appServices'
              properties: {
                serviceName: 'Microsoft.Web/serverfarms'
              }
            }
          ]
          serviceEndpoints: []
          addressPrefix: '10.0.1.0/24'
        }
      }
    ]
  }
  resource subnetForDb 'subnets' existing = {
    name: 'database-subnet'
  }
  resource subnetForApp 'subnets' existing = {
    name: 'webapp-subnet'
  }
  resource subnetForCache 'subnets' existing = {
    name: 'cache-subnet'
  }
}

resource privateDnsZoneDB 'Microsoft.Network/privateDnsZones@2020-06-01' = {
  name: 'privatelink.mysql.database.azure.com'
  location: 'global'
  dependsOn: [
    virtualNetwork
  ]
}

resource privateDnsZoneLinkDB 'Microsoft.Network/privateDnsZones/virtualNetworkLinks@2020-06-01' = {
  parent: privateDnsZoneDB
  name: '${appName}-dblink'
  location: 'global'
  properties: {
    virtualNetwork: {
      id: virtualNetwork.id
    }
    registrationEnabled: false
  }
}

resource privateDnsZoneCache 'Microsoft.Network/privateDnsZones@2020-06-01' = {
  name: 'privatelink.redis.cache.windows.net'
  location: 'global'
  dependsOn: [
    virtualNetwork
  ]
}

resource privateDnsZoneLinkCache 'Microsoft.Network/privateDnsZones/virtualNetworkLinks@2020-06-01' = {
  parent: privateDnsZoneCache
  name: '${appName}-cachelink'
  location: 'global'
  properties: {
    virtualNetwork: {
      id: virtualNetwork.id
    }
    registrationEnabled: false
  }
}

resource dbserver 'Microsoft.DBforMySQL/flexibleServers@2023-06-30' = {
  location: location
  name: '${appName}-mysql-server'
  tags: null
  properties: {
    version: '8.0.21'
    administratorLogin: 'mysqladmin'
    administratorLoginPassword: databasePassword
    storage: {
      storageSizeGB: 128
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    network: {
      privateDnsZoneResourceId: privateDnsZoneDB.id
      delegatedSubnetResourceId: virtualNetwork::subnetForDb.id
      publicNetworkAccess: 'Disabled'
    }
  }
  sku: {
    name: 'Standard_D2ds_v4'
    tier: 'GeneralPurpose'
  }
  dependsOn: [
    privateDnsZoneLinkDB
  ]
}

resource db 'Microsoft.DBforMySQL/flexibleServers/databases@2023-06-30' = {
  parent: dbserver
  name: '${appName}-mysql-database'
}

resource cachePrivateEndpoint 'Microsoft.Network/privateEndpoints@2023-04-01' = {
  name: '${appName}-cache-privateEndpoint'
  location: location
  properties: {
    subnet: {
      id: virtualNetwork::subnetForCache.id
    }
    privateLinkServiceConnections: [
      {
        name: '${appName}-cache-privateEndpoint'
        properties: {
          privateLinkServiceId: redisCache.id
          groupIds: ['redisCache']
        }
      }
    ]
  }
  resource cachePrivateDnsZoneGroup 'privateDnsZoneGroups' = {
    name: 'default'
    properties: {
      privateDnsZoneConfigs: [
        {
          name: 'privatelink-redis-cache-windows-net'
          properties: {
            privateDnsZoneId: privateDnsZoneCache.id
          }
        }
      ]
    }
  }
}



resource redisCache 'Microsoft.Cache/Redis@2023-08-01' = {
  name: '${appName}-cache'
  location: location
  tags: {}
  properties: {
    sku: {
      name: 'Standard'
      family: 'C'
      capacity: 1
    }
    redisConfiguration: {}
    enableNonSslPort: false
    redisVersion: '6'
    publicNetworkAccess: 'Disabled'
  }
}

resource appServicePlan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: '${appName}-plan'
  location: location
  kind: 'linux'
  tags: null
  properties: {
    reserved: true
  }
  sku: {
    name: 'B1'
  }
}

resource web 'Microsoft.Web/sites@2022-09-01' = {
  name: appName
  location: location
  tags: union(tags, { 'azd-service-name': 'web' })
  properties: {
    siteConfig: {
      linuxFxVersion: 'TOMCAT|10.0-java17'
      appSettings: []
      vnetRouteAllEnabled: true
      ftpsState: 'Disabled'
    }
    serverFarmId: appServicePlan.id
    httpsOnly: true
  }

  resource logs 'config' = {
    name: 'logs'
    properties: {
      applicationLogs: {
        fileSystem: {
          level: 'Verbose'
        }
      }
      detailedErrorMessages: {
        enabled: true
      }
      failedRequestsTracing: {
        enabled: true
      }
      httpLogs: {
        fileSystem: {
          enabled: true
          retentionInDays: 1
          retentionInMb: 35
        }
      }
    }
  }

  resource appSettings 'config' = {
    name: 'appsettings'
    properties: {
    }
  }

  resource webappVnetConfig 'networkConfig' = {
    name: 'virtualNetwork'
    properties: {
      subnetResourceId: virtualNetwork::subnetForApp.id
    }
  }
  
  dependsOn: [virtualNetwork]
}

resource dbConnector 'Microsoft.ServiceLinker/linkers@2022-05-01' = {
  scope: web
  name: 'defaultConnector'
  properties: {
    targetService: {
      type: 'AzureResource'
      id: db.id
    }
    authInfo: {
      authType: 'secret'
      name: 'mysqladmin'
      secretInfo: {
        secretType: 'rawValue'
        value: databasePassword
      }
    }
    clientType: 'java'
    vNetSolution: null
  }
}

resource cacheConnector 'Microsoft.ServiceLinker/linkers@2022-05-01' = {
  scope: web
  name: 'RedisConnector'
  properties: {
    targetService: {
      type: 'AzureResource'
      id:  resourceId('Microsoft.Cache/Redis/Databases', redisCache.name, '0')
    }
    authInfo: {
      authType: 'secret'
    }
    clientType: 'java'
    vNetSolution: {
      type: 'privateLink'
    }
  }
}

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2020-03-01-preview' = {
  name: '${appName}-workspace'
  location: location
  tags: tags
  properties: any({
    retentionInDays: 30
    features: {
      searchVersion: 1
    }
    sku: {
      name: 'PerGB2018'
    }
  })
}

resource webdiagnostics 'Microsoft.Insights/diagnosticSettings@2021-05-01-preview' = {
  name: 'AllLogs'
  scope: web
  properties: {
    workspaceId: logAnalyticsWorkspace.id
    logs: [
      {
        category: 'AppServiceHTTPLogs'
        enabled: true
      }
      {
        category: 'AppServiceConsoleLogs'
        enabled: true
      }
      {
        category: 'AppServiceAppLogs'
        enabled: true
      }
      {
        category: 'AppServiceAuditLogs'
        enabled: true
      }
      {
        category: 'AppServiceIPSecAuditLogs'
        enabled: true
      }
      {
        category: 'AppServicePlatformLogs'
        enabled: true
      }
    ]
    metrics: [
      {
        category: 'AllMetrics'
        enabled: true
      }
    ]
  }
}

output WEB_URI string = 'https://${web.properties.defaultHostName}'

resource webAppSettings 'Microsoft.Web/sites/config@2022-03-01' existing = {
  name: web::appSettings.name
  parent: web
}

var webAppSettingsKeys = map(items(webAppSettings.list().properties), setting => setting.key)
output WEB_APP_SETTINGS array = webAppSettingsKeys
output WEB_APP_LOG_STREAM string = format('https://portal.azure.com/#@/resource{0}/logStream', web.id)
output WEB_APP_SSH string = format('https://{0}.scm.azurewebsites.net/webssh/host', web.name)
output WEB_APP_CONFIG string = format('https://portal.azure.com/#@/resource{0}/configuration', web.id)
