# Sistema de Análise de Sintomas em pacientes

Aplicação backend dividida em microserviços para cadastro de usuários, recepção de sintomas e geração de diagnósticos preliminares.

## Estrutura do projeto

- `user-service`: responsável pelo cadastro e gerenciamento de usuários
- `symptomservice`: recebe os sintomas, valida o usuário e coordena a comunicação entre os serviços
- `ai-service`: processa os sintomas e retorna uma resposta de diagnóstico

## Portas padrao

- `user-service`: `8080`
- `symptomservice`: `8081`
- `ai-service`: `8082`

## Tecnologias utilizadas

- Java 25
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Lombok
- Ollama

## Como executar

Inicie cada serviço em um terminal separado, à partir da raiz de cada módulo.
Utilize o Postman para gerar os sintomas do paciente simulado.

```bash
./mvnw spring-boot:run
```

## Como testar

Crie um usuário utilizando o método `POST` na URL `http://localhost:8080/users` e envie um JSON

```json
{
  "name": "Teste",
  "email": "teste@local.com"
}
```

Envie o sistoma utilizando o método `POST` na URL `http://localhost:8081/symptoms` e envie um JSON

```json
{
  "userId": 1,
  "symptoms": ["tosse"]
}
```

Também é possível testar apenas o diagnóstico utilizando método `POST` na URL `http://localhost:8082/ai/diagnos` com o JSON

```json
{
  "symptoms": ["tosse"]
}
```

## Fluxo da aplicacao

1. O usuário é cadastrado no `user-service`
2. Os sintomas são enviados ao `symptomservice`
3. O `symptomservice` consulta o servico de diagnostico
4. A resposta consolidada e retornada ao cliente
