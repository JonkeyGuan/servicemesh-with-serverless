# migrate-spring-cloud-to-istio

## Test on local
```
curl -X POST -H "Content-Type: application/json" http://localhost:8083/application -d @cardApplication.json
```

## Test on OpenShift
### Suppose:
* Openshift Base Domain: apps.ocp.example.com
* Applicaion Project Name: servicemesh-with-serverless

#### If you have a diffent domain, please run below to replace them all
```
grep -rl 'ocp' . | grep -E '.yaml|.md' | xargs sed -i "" 's/ocp.example.com/ocp.xxx.com/g'
```

### Test

```
curl -X POST -H "Content-Type: application/json" http://card.sm.apps.ocp.example.com/application -d @cardApplication.json

siege -c 4  -r 20 -v -T "application/json" "http://card.sm.apps.ocp.example.com/application POST < cardApplication.json"
```

### Create a network policy permits traffic flow from Knative system Pods to Knative services

```
oc label namespace knative-serving serving.knative.openshift.io/system-namespace=true
oc label namespace knative-serving-ingress serving.knative.openshift.io/system-namespace=true

cat <<EOF | oc apply -f -
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-from-serving-system-namespace
  namespace: servicemesh-with-serverless
spec:
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          serving.knative.openshift.io/system-namespace: "true"
  podSelector: {}
  policyTypes:
  - Ingress
EOF
```
