# Desenvolvimento Backend

Código final da aula 02 - Persistência

## Arquivos modificados
- build.gradle.kts: Adicionada a biblioteca do JPA e configurado o plugin
- application.yaml: Configurações do JPA

## Classes modificadas
- User: Mapeamento do usuário no JPA
- UserRepository: Alterado para usar o JPA
- UserService: Inserção com validação do email único
- UserController