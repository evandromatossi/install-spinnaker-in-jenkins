#bin/bash
set -x
# Create nodePort spin-gate spinnaker
kubectl -n spinnaker expose service spin-gate --type NodePort \
  --port 8084 \
  --target-port 8084 \
  --name spin-gate-nodeport
sleep 10
# Create nodePort spin-deck spinnaker
kubectl -n spinnaker expose service spin-deck --type NodePort \
  --port 9000 \
  --target-port 9000 \
  --name spin-deck-nodeport