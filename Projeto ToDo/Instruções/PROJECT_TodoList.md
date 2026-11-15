# TodoList — Documento Mestre do Projeto

> **Documento de referência para desenvolvimento com IA.**  
> Leia este arquivo por completo antes de iniciar qualquer implementação. Ele define o objetivo, as regras, as etapas e os critérios de conclusão do projeto.

## 1. Identificação do projeto

**Nome:** TodoList  
**Disciplina:** Programação Mobile  
**Instituição:** UNASP-SP  
**Semestre:** 2026/2  
**Tecnologias principais:** Kotlin e Jetpack Compose  
**Plataforma inicial:** Android

### Criadores

- Guilherme Oliveira
- Kaue Bueno
- Pedro Henrique Gomes
- Vinicius Borges
- Lucas Cardoso
- Henrique Gomes

## 2. Visão do produto

O TodoList é um aplicativo mobile de organização pessoal. Seu propósito é reunir tarefas, prazos, categorias, lembretes e acompanhamento de produtividade em um só lugar, ajudando o usuário a organizar sua rotina e não esquecer compromissos.

O aplicativo deve ter uma experiência visual moderna, simples, intuitiva e consistente, tomando como referência o protótipo criado no Figma Make indicado pela equipe e o material de apresentação do projeto.

**Referência visual:**  
https://share.google/Pyql1SzUiTuuUIj6f

Se a referência não estiver acessível, não invente detalhes sobre suas telas. Solicite imagens, exportação ou descrição do protótipo aos integrantes antes de definir diferenças visuais importantes.

### Problema que o app busca resolver

Tarefas ficam espalhadas em diferentes aplicativos e anotações, prazos podem ser esquecidos e o usuário perde a visão geral do que precisa fazer.

### Solução proposta

Oferecer um espaço central para criar, editar, categorizar, priorizar e concluir tarefas, visualizar compromissos no calendário e acompanhar o progresso. Em etapas posteriores, permitir a sincronização com serviços externos, como ClickUp e Google Agenda.

## 3. Público-alvo

Estudantes, profissionais e qualquer pessoa que queira organizar melhor suas tarefas e compromissos.

## 4. Objetivos e funcionalidades

### 4.1 Funcionalidades essenciais do MVP

1. **Tela inicial / Dashboard**
   - Resumo das tarefas do dia.
   - Tarefas pendentes, concluídas e atrasadas.
   - Acesso rápido à criação de tarefa.
   - Visão resumida do progresso.

2. **Tarefas**
   - Criar, visualizar, editar e excluir tarefas.
   - Definir título e descrição.
   - Definir data e horário de vencimento.
   - Definir prioridade.
   - Associar uma categoria.
   - Marcar como concluída e reabrir, quando necessário.
   - Pesquisar e filtrar tarefas.

3. **Categorias**
   - Criar, editar e excluir categorias.
   - Associar cor ou identificação visual.
   - Filtrar tarefas por categoria.

4. **Calendário**
   - Visualização de tarefas por data.
   - Seleção de um dia para consultar seus compromissos.
   - Diferenciação visual entre tarefas concluídas e pendentes.

5. **Estatísticas**
   - Quantidade de tarefas criadas e concluídas.
   - Progresso por período.
   - Resumo por categoria.
   - Apresentação clara, sem métricas enganosas.

6. **Configurações**
   - Preferências do aplicativo.
   - Opções de notificações.
   - Área de integrações externas.
   - Informações sobre o aplicativo e seus criadores.

### 4.2 Funcionalidades posteriores

Estas funcionalidades não devem atrasar a entrega do MVP. Só devem ser iniciadas depois que as funcionalidades essenciais estiverem implementadas e testadas.

- Sincronização com Google Agenda.
- Integração com ClickUp.
- Possibilidade de adicionar outros serviços no futuro.
- Importação e exportação de tarefas, quando viável.
- Filtros e recursos avançados de produtividade.

## 5. Integrações externas

O objetivo é permitir que o TodoList se conecte a outros serviços de organização, evitando que o usuário precise cadastrar manualmente as mesmas informações em vários lugares.

### Google Agenda

A integração deverá avaliar o uso das APIs oficiais do Google Calendar e do fluxo de autenticação apropriado.

Possíveis recursos:
- Consultar eventos autorizados pelo usuário.
- Criar eventos a partir de tarefas, mediante confirmação.
- Atualizar ou remover eventos criados pelo aplicativo, respeitando as permissões.
- Informar claramente o estado da sincronização.

### ClickUp

A integração deverá utilizar a API oficial do ClickUp e o mecanismo de autenticação suportado por ela.

Possíveis recursos:
- Conectar uma conta autorizada.
- Consultar tarefas e listas permitidas.
- Criar ou atualizar tarefas, conforme as permissões disponíveis.
- Identificar a origem dos itens sincronizados.

### Regras obrigatórias para integrações

- Não simular uma integração como se fosse real.
- Não armazenar senhas, tokens ou chaves secretas diretamente no código-fonte.
- Solicitar autorização do usuário antes de acessar dados externos.
- Explicar quais dados serão acessados e para qual finalidade.
- Tratar falhas de rede, expiração de sessão e permissões negadas.
- Evitar duplicação de tarefas e eventos.
- Definir claramente qual serviço é a origem de cada item.
- Não sobrescrever alterações externas sem uma regra de sincronização documentada.
- Documentar limitações das APIs e recursos que dependam de configuração externa.

**Importante:** a equipe deverá configurar credenciais e projetos de desenvolvedor nos serviços correspondentes. A IA não deve inventar chaves, credenciais ou resultados de autenticação.

## 6. Tecnologias e arquitetura

### Obrigatório

- Kotlin como linguagem de programação.
- Jetpack Compose para a interface.
- Android Studio e Gradle.
- Projeto Android nativo.

### Diretrizes técnicas

Adote uma arquitetura organizada e adequada ao tamanho do projeto. Como ponto de partida, considere:

- **MVVM:** separação entre interface, estado e lógica de apresentação.
- **ViewModel:** gerenciamento do estado das telas e comunicação com os casos de uso/repositórios.
- **Repository:** abstração das fontes de dados.
- **Room:** armazenamento local das tarefas e categorias.
- **DataStore:** preferências simples do usuário.
- **Navigation Compose:** navegação entre telas.
- **WorkManager:** trabalhos em segundo plano quando forem realmente necessários.
- **Coroutines e Flow:** operações assíncronas e observação de dados.

A IA poderá propor ajustes técnicos, mas deverá explicar a necessidade e registrar a decisão neste documento antes de alterar significativamente a arquitetura.

### Organização sugerida

A estrutura exata poderá ser ajustada durante a implementação, mantendo separação de responsabilidades.

```text
app/
└── src/main/
    ├── java/.../
    │   ├── data/
    │   │   ├── local/
    │   │   ├── remote/
    │   │   ├── repository/
    │   │   └── model/
    │   ├── domain/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── usecase/
    │   ├── ui/
    │   │   ├── components/
    │   │   ├── navigation/
    │   │   ├── screens/
    │   │   └── theme/
    │   ├── integrations/
    │   │   ├── googlecalendar/
    │   │   └── clickup/
    │   └── MainActivity.kt
    └── AndroidManifest.xml
```

Não crie pastas ou camadas vazias apenas para seguir o exemplo. A estrutura deve refletir o código que realmente existe.

## 7. Ambiente de desenvolvimento e conexão com GitHub

### Android Studio como ambiente obrigatório

Todo o desenvolvimento do aplicativo deverá ser realizado no **Android Studio**, utilizando Kotlin e Jetpack Compose.

A IA deverá trabalhar diretamente no projeto Android aberto no Android Studio, criando e editando os arquivos do projeto, configurando dependências e executando as verificações disponíveis no ambiente.

- Não desenvolver uma versão paralela do aplicativo em outra plataforma.
- Não substituir o Android Studio por um ambiente web ou por outra IDE.
- Manter o projeto compatível com a estrutura e as ferramentas oficiais do Android.
- Quando não tiver acesso direto ao Android Studio ou ao sistema de arquivos, explicar essa limitação e orientar a equipe sobre como fornecer o acesso necessário. Não afirmar que alterou ou executou arquivos sem realmente ter feito isso.

### Repositório existente no GitHub

O projeto da equipe **já possui um repositório no GitHub**. Antes de iniciar qualquer implementação ou conexão com controle de versão, a IA deverá pedir à equipe o link/URL desse repositório.

Após receber o endereço:

1. Confirmar qual é o repositório correto.
2. Verificar se o ambiente possui acesso ao GitHub e as permissões necessárias.
3. Conectar ou clonar o repositório no ambiente de desenvolvimento, conforme o estado atual do projeto.
4. Verificar a estrutura, a branch ativa, o histórico e os arquivos existentes.
5. Conferir se há alterações locais antes de atualizar, trocar de branch ou sincronizar arquivos.
6. Preservar o código e o histórico já existentes; não sobrescrever nem apagar arquivos sem autorização.
7. Identificar se o projeto Android já está configurado no repositório ou se precisa ser criado dentro dele.

### Regras para operações Git

- Não presumir o endereço, nome ou proprietário do repositório.
- Não solicitar nem armazenar senha pessoal do GitHub. Utilizar os mecanismos seguros de autenticação autorizados pela equipe.
- Não publicar tokens, chaves ou credenciais no código ou nos arquivos Markdown.
- Não executar `push`, criar branches, abrir pull requests, mesclar branches ou alterar configurações do repositório sem autorização explícita da equipe.
- Antes de operações que possam sobrescrever alterações, explicar o que será feito e pedir confirmação.
- Ao concluir uma etapa, informar quais arquivos foram alterados e, quando aplicável, quais comandos Git foram executados.
- Registrar neste documento a branch de trabalho e as decisões relevantes de versionamento, sem incluir segredos.

**Primeira ação obrigatória:** pedir à equipe a URL do repositório GitHub existente e confirmar o acesso antes de começar a implementar o aplicativo.

## 8. Diretrizes de interface e experiência

- Respeitar a identidade visual do protótipo de referência, quando ele estiver disponível.
- Manter consistência entre cores, tipografia, ícones, espaçamentos e componentes.
- Priorizar uma interface limpa e fácil de entender.
- Usar componentes reutilizáveis no Jetpack Compose.
- Garantir boa adaptação a diferentes tamanhos de tela.
- Exibir estados vazios, carregamento, sucesso e erro.
- Incluir mensagens de confirmação para ações importantes.
- Não depender apenas de cores para comunicar prioridade ou status.
- Manter textos em português do Brasil, salvo decisão documentada da equipe.
- Evitar telas decorativas sem funcionalidade real.

## 9. Etapas obrigatórias de desenvolvimento

A implementação deve seguir esta sequência. Não avance silenciosamente para uma etapa posterior deixando a anterior incompleta.

### Etapa 0 — Análise e planejamento

- [ ] Analisar este documento.
- [ ] Analisar o protótipo do Figma Make e os materiais fornecidos.
- [ ] Identificar telas, componentes e fluxos.
- [ ] Definir a arquitetura inicial.
- [ ] Definir o escopo do MVP.
- [ ] Registrar dúvidas e dependências.

**Critério de conclusão:** plano de telas, arquitetura e escopo documentados e validados pela equipe.

### Etapa 1 — Configuração do projeto

- [ ] Criar ou revisar o projeto Android.
- [ ] Configurar Kotlin, Jetpack Compose e Gradle.
- [ ] Configurar tema e identidade visual inicial.
- [ ] Organizar a estrutura de pacotes.
- [ ] Confirmar que o aplicativo compila e abre.

**Critério de conclusão:** projeto executável, sem erros de compilação.

### Etapa 2 — Estrutura visual e navegação

- [ ] Implementar navegação principal.
- [ ] Criar componentes reutilizáveis.
- [ ] Construir a Dashboard.
- [ ] Construir as telas de tarefas, calendário, estatísticas e configurações.
- [ ] Conferir consistência visual com a referência.

**Critério de conclusão:** navegação funcional e telas principais implementadas, mesmo que ainda usem dados temporários.

### Etapa 3 — Funcionalidades de tarefas

- [ ] Criar modelo de tarefa.
- [ ] Implementar criação e edição.
- [ ] Implementar exclusão com confirmação.
- [ ] Implementar conclusão e reabertura.
- [ ] Implementar prioridade, categoria e prazo.
- [ ] Implementar pesquisa e filtros.

**Critério de conclusão:** operações de tarefas funcionando e testadas.

### Etapa 4 — Persistência local

- [ ] Configurar banco local com Room.
- [ ] Criar entidades, DAOs e banco de dados.
- [ ] Integrar o banco aos repositórios.
- [ ] Garantir que as tarefas permaneçam após fechar e abrir o app.
- [ ] Tratar erros de leitura e gravação.

**Critério de conclusão:** dados persistidos corretamente no dispositivo.

### Etapa 5 — Calendário, lembretes e estatísticas

- [ ] Exibir tarefas no calendário.
- [ ] Permitir consulta por data.
- [ ] Implementar notificações e lembretes, respeitando as permissões do Android.
- [ ] Implementar estatísticas com dados reais do aplicativo.
- [ ] Testar datas, horários e mudanças de fuso quando aplicável.

**Critério de conclusão:** recursos funcionando com os dados persistidos.

### Etapa 6 — Integrações externas

- [ ] Pesquisar e validar os requisitos atuais das APIs oficiais.
- [ ] Definir o fluxo de autorização do Google Agenda.
- [ ] Implementar e testar a integração com Google Agenda.
- [ ] Definir o fluxo de autorização do ClickUp.
- [ ] Implementar e testar a integração com ClickUp.
- [ ] Tratar sincronização, conflitos, duplicações e erros.
- [ ] Documentar configurações externas necessárias.

**Critério de conclusão:** cada integração anunciada no aplicativo deve ter sido testada com uma conta autorizada. Se alguma integração não puder ser concluída, ela deverá aparecer como pendente, não como funcional.

### Etapa 7 — Testes e correções

- [ ] Testar os fluxos principais.
- [ ] Testar persistência após reiniciar o aplicativo.
- [ ] Testar campos vazios e entradas inválidas.
- [ ] Testar permissões concedidas e negadas.
- [ ] Testar ausência de internet.
- [ ] Testar falhas nas integrações.
- [ ] Corrigir problemas de interface e navegação.
- [ ] Verificar acessibilidade e mensagens de erro.
- [ ] Executar build final.

**Critério de conclusão:** funcionalidades do escopo final testadas, problemas conhecidos registrados e build concluído.

### Etapa 8 — Finalização e documentação explicativa

Esta etapa só poderá começar depois da conclusão das etapas anteriores e da validação final do aplicativo.

- [ ] Atualizar o status geral do projeto para concluído.
- [ ] Conferir se o código está organizado.
- [ ] Criar `DOCUMENTACAO_FINAL.md`.
- [ ] Explicar separadamente os principais arquivos e códigos.
- [ ] Descrever a responsabilidade de cada classe e componente importante.
- [ ] Explicar os principais fluxos de dados.
- [ ] Explicar a arquitetura e as decisões técnicas.
- [ ] Explicar como executar o projeto.
- [ ] Documentar integrações, configurações e limitações.
- [ ] Registrar testes realizados.

**Regra essencial:** não produzir a explicação detalhada de cada código principal durante as etapas de implementação. Durante o desenvolvimento, comentários técnicos pontuais e registros necessários são permitidos, mas a documentação didática completa deverá ser entregue somente ao final do projeto.

## 10. Regras de trabalho para a IA

Estas regras devem ser seguidas em todas as interações de desenvolvimento.

1. Leia este documento antes de executar uma nova tarefa.
2. Trabalhe por etapas e em pequenas entregas verificáveis.
3. Não implemente todo o aplicativo de uma só vez.
4. Antes de alterar arquivos, entenda a estrutura e o código existente.
5. Não reescreva partes funcionais sem necessidade.
6. Preserve decisões já aprovadas pela equipe.
7. Não invente funcionalidades concluídas, testes executados ou integrações funcionais.
8. Não substitua Kotlin ou Jetpack Compose por outra tecnologia.
9. Não use dados temporários como se fossem dados reais.
10. Quando houver dúvida que afete o escopo ou a experiência, pergunte à equipe antes de decidir.
11. Ao terminar uma tarefa, informe os arquivos criados ou alterados, o que foi feito e como validar.
12. Se encontrar um erro, explique a causa provável e corrija de forma controlada.
13. Não avance de etapa sem verificar os critérios de conclusão.
14. Não exponha credenciais, tokens ou dados pessoais em logs ou no repositório.
15. Mantenha o aplicativo executável sempre que possível.
16. Use um modelo de verificação que tenha um Junior que faz todo o serviço, o pleno verifica e o que tiver que melhorar ele melhora e passa para o senior verificar tudo e validar.

## 11. Protocolo obrigatório de atualização deste Markdown

Este arquivo é a fonte central de acompanhamento do projeto. **Toda vez que uma tarefa for concluída ou uma decisão relevante mudar, atualize este próprio documento.**

Ao atualizar:

- Marque como concluídos somente itens realmente implementados e verificados.
- Atualize a etapa atual e o percentual de progresso estimado.
- Registre decisões técnicas e mudanças de escopo.
- Registre problemas encontrados e dependências externas.
- Atualize o histórico de alterações.
- Preserve as informações anteriores que ainda forem relevantes.
- Não apague etapas pendentes para aparentar progresso.
- Se não for possível editar o arquivo diretamente, apresente o trecho atualizado e informe que a atualização ainda precisa ser aplicada ao arquivo.

### Estado atual

- **Etapa atual:** Etapa 0 — Análise e planejamento.
- **Status geral:** Planejamento iniciado.
- **Progresso:** 0% de implementação.
- **Última atualização:** 15/11/2026.
- **Pendências principais:** validar o protótipo visual, fechar o escopo do MVP e definir a arquitetura inicial.

### Histórico de alterações

| Data | Alteração |
|---|---|
| 15/11/2026 | Criação do documento mestre, definição do escopo inicial, etapas e regras de desenvolvimento. |
| 15/11/2026 | Inclusão do Android Studio como ambiente obrigatório e do procedimento para solicitar e conectar o repositório GitHub existente. |

## 12. Critérios gerais de aceite

O projeto será considerado concluído quando:

- O aplicativo Android estiver implementado em Kotlin e Jetpack Compose.
- As funcionalidades essenciais do MVP estiverem operacionais.
- As tarefas e categorias forem persistidas localmente.
- Calendário, lembretes e estatísticas estiverem funcionando conforme o escopo aprovado.
- As integrações anunciadas como concluídas tiverem sido testadas de verdade.
- Os fluxos principais tiverem sido testados.
- Não houver erros críticos conhecidos impedindo o uso do aplicativo.
- O projeto estiver organizado e executável.
- A documentação explicativa final tiver sido criada separadamente.

## 13. Instrução inicial para a IA

Comece pela **Etapa 0 — Análise e planejamento**.

**Antes de qualquer implementação, peça à equipe a URL do repositório GitHub existente.** Confirme o acesso, conecte ou clone o repositório com segurança e inspecione o projeto atual. Não invente o endereço e não faça operações de publicação ou alterações destrutivas sem autorização.

Todo o desenvolvimento deverá ocorrer no **Android Studio**, diretamente no projeto Android da equipe.

Primeiro, analise a estrutura atual do projeto e os materiais de referência disponíveis. Não comece a implementar telas ou funcionalidades antes de apresentar um plano curto com:

1. O que já existe no projeto.
2. O que ainda precisa ser criado.
3. A arquitetura proposta.
4. A sequência de implementação.
5. Dúvidas ou materiais que precisam ser fornecidos pela equipe.

Depois da validação da equipe, execute as etapas gradualmente. Ao concluir cada tarefa, atualize este `PROJECT.md`, respeitando o protocolo da seção 11.

**Lembre-se:** a documentação didática, explicando individualmente os principais códigos e arquivos, será produzida somente na Etapa 8, após a finalização e os testes de todo o aplicativo.
**Observações:** 