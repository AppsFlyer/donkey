#!/usr/bin/env sh

set -ev

mvn install jacoco:report coveralls:report -DdryRun=true -B -V
lein do kibit, coveralls, run -m coveralls-report
curl -F 'json_file=@target/coveralls/coveralls.json' 'https://coveralls.io/api/v1/jobs'
