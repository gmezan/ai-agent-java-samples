# AKS OpenAI MSI

https://learn.microsoft.com/en-us/azure/aks/workload-identity-deploy-cluster

```bash
RESOURCE_GROUP=<RESOURCE_GROUP>
CLUSTER_NAME=<CLUSTER_NAME>
LOCATION=eastus2
SUBSCRIPTION=<SUBSCRIPTION>
OPENAI_NAME=<OPENAI_NAME>

# Enable OIDC and workload identity
az aks update --name $CLUSTER_NAME \
              --resource-group $RESOURCE_GROUP \
              --enable-oidc-issuer \
              --enable-workload-identity

# Get the cluster OIDC 
export AKS_OIDC_ISSUER="$(az aks show --name $CLUSTER_NAME --resource-group $RESOURCE_GROUP --query "oidcIssuerProfile.issuerUrl" --output tsv)"

# Create a Managed Identity
export USER_ASSIGNED_IDENTITY_NAME="id-app-openai-msi"
az identity create \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --location "${LOCATION}" \
    --subscription "${SUBSCRIPTION}"
export USER_ASSIGNED_CLIENT_ID="$(az identity show \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --query 'clientId' \
    --output tsv)"

# Create the federated identity
export FEDERATED_IDENTITY_CREDENTIAL_NAME="aks-federated-id"
export SERVICE_ACCOUNT_NAMESPACE=default
export SERVICE_ACCOUNT_NAME=app-openai-msi-sa
az identity federated-credential create \
    --name ${FEDERATED_IDENTITY_CREDENTIAL_NAME} \
    --identity-name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --issuer "${AKS_OIDC_ISSUER}" \
    --subject system:serviceaccount:"${SERVICE_ACCOUNT_NAMESPACE}":"${SERVICE_ACCOUNT_NAME}" \
    --audience api://AzureADTokenExchange
    

# deploy application
export OPENAI_MODEL=<OPENAI_MODEL>
export OPENAI_ENDPOINT=<OPENAI_ENDPOINT>

envsubst < manifest.yml | k apply -f -


# Assign the role
export IDENTITY_PRINCIPAL_ID=$(az identity show \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query principalId \
    --output tsv)

export AZURE_OPENAI_ID=$(az cognitiveservices account show --resource-group "${RESOURCE_GROUP}" \
    --name "${OPENAI_NAME}" \
    --query id \
    --output tsv)

az role assignment create \
    --assignee-object-id "${IDENTITY_PRINCIPAL_ID}" \
    --role "Cognitive Services OpenAI User" \
    --scope "${AZURE_OPENAI_ID}" \
    --assignee-principal-type ServicePrincipal
    
```