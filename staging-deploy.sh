#!/bin/bash          
echo Deploy DevNexus to Staging
cf delete devnexus-old
cf delete devnexus
mvn cf:push
cf app devnexus
