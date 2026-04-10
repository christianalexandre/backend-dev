# Desenvolvimento Backend

Nesta aula, adicionamos um serviço para inserção de papéis e formas de vincular o usuário a seu papel.

## Arquivos modificados
- build.gradle.kts: Adicionada a biblioteca do JPA e configurado o plugin
- application.yaml: Configurações do JPA

## Classes modificadas
- User: Mapeamento do usuário no JPA, lista de papéis
- UserRepository: Alterado para usar o JPA
- UserService: Inserção com validação do email único
- UserController

## Classes adicionadas
- Role: Papel possível para o usuário
- RoleRepository, RoleService e RoleController: Classes MVC do papel, similar as do usuário