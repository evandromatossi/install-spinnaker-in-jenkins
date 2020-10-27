# Instalacao spinnaker google cloud plataform
by: Evandromatos.bmsix.com

# Create ingres 

Criar ingress com os services spin-deck-nodeport e spin-gate-nodeport, conforme exemplo abaixo.

link de acesso painel gcp ingress

https://console.cloud.google.com/kubernetes/discovery?organizationId=961329749118&project=bmsix-labs&pageState=(%22savedViews%22:(%22i%22:%22dd99d622a31e4ad38aa9b896ed401db8%22,%22c%22:%5B%5D,%22n%22:%5B%5D))

# Exemplo ingress
apiVersion: "extensions/v1beta1"
kind: "Ingress"
metadata:
  name: "ingress-nodeport-spinnaker"
  namespace: "spinnaker"
spec:
  rules:
  - http:
      paths:
      - backend:
          serviceName: "spin-deck-nodeport"
          servicePort: 9000
    host: "spinnaker-template.com.br"
  - http:
      paths:
      - backend:
          serviceName: "spin-gate-nodeport"
          servicePort: 8084
    host: "spinnaker-template-api.com.br"


    # Ajustar Health checks gcp 

    Entrar no Health checks que esta com problema e colocar no patch o /healthz ou /health

#  Override spinnaker 
   hal config security api edit --override-base-url https://spinnaker-template-api.com.br
   hal config security ui edit --override-base-url https://spinnaker-template.com.br
   hal deploy apply