# SCARF-ModelingEnvironment

SCARF-ModelingEnvironment is the modeling tool used for modeling and discovering reusable cloud application topologies. It is based on [Winery](https://github.com/eclipse/winery). This tool is connected with the SCARF services covering different features of SCARF.

# Run
1. Download and install Eclipse Kepler
2. Go to [SCARF](https://github.com/sgomezsaez/SCARF) and get the docker cluser up and running
3. A copy of the winery repo is located in `winery_repo_copy`. Go to `./winery/org.eclipse.winery.repository/src/main/resources/winery.properties` and specify the absolute path of the `./winery_repo_copy` in the property `repositoryPath`
4. Go to `./winery` for building and running the modeling environment
