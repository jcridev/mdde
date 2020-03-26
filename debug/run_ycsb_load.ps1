$loc=Get-Location
$ycsb_ver="ycsb-0.18.0-SNAPSHOT"

New-Item -ItemType Directory -Force -Path .\ycsb\
Clear-Content $loc\ycsb\
tar -xvzf ..\..\YCSB\distribution\target\$ycsb_ver.tar.gz -C .\ycsb\
Set-Location .\ycsb\$ycsb_ver\bin\
& .\ycsb.bat load mdde.redis -P $loc\ycsb\$ycsb_ver\workloads\workloadc -p mdde.redis.configfile=$loc\ycsb_client_config.yml -p verbose=true
Set-Location $loc