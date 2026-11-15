# TodoList — Acompanhamento do Projeto

> Complemento do `PROJECT_TodoList.md` (documento mestre, que permanece como instruções).
> Por decisão da equipe (15/11/2026), as atualizações de progresso, decisões e histórico são registradas **neste arquivo**.

## Estado atual

- **Etapa atual:** Etapas 3 e 4 concluídas (juntas — ver D12) · próxima: Etapa 5 (lembretes, feriados via BrasilAPI/Ktor).
- **Status geral:** App funcional com login/cadastro local, tarefas e categorias por usuário salvas em Room, tema black.
- **Progresso:** ~60% de implementação.
- **Última atualização:** 15/11/2026.
- **Pendências principais:**
  - Etapa 5: notificações/lembretes (o interruptor está desabilitado, marcado "disponível em breve"), feriados da BrasilAPI no calendário (Ktor).
  - Etapa 7: testes instrumentados / mais casos de erro.

## Decisões da equipe

| # | Data | Decisão |
|---|---|---|
| D1 | 15/11/2026 | Repositório: `github.com/BG0010/programa-o-mobile`, projeto dentro da pasta `Projeto ToDo`. Branch de trabalho: `main`. |
| D2 | 15/11/2026 | Pacote / applicationId: `com.todolist.app` (criado do zero, sem reaproveitar pacote anterior). |
| D3 | 15/11/2026 | Arquitetura aprovada: MVVM + Repository, Room, DataStore, Navigation Compose (Bottom Navigation), Coroutines/Flow, Material 3. |
| D4 | 15/11/2026 | Evitar API externa sempre que possível; **nenhuma API paga**. Integrações Google Agenda/ClickUp ficam fora até nova decisão. |
| D5 | 15/11/2026 | Atualizações de progresso registradas neste arquivo em vez do documento mestre. |
| D7 | 15/11/2026 | Nome exibido no app: **TodoList** (o protótipo usa "Prazo Certo"; apenas o visual foi seguido). |
| D8 | 15/11/2026 | Categorias: 4 padrão do protótipo (Estudos, Trabalho, Pessoal, Casa) + tela em Ajustes para criar, editar e excluir. |
| D9 | 15/11/2026 | Perfil: apenas nome local editável (DataStore), sem e-mail e sem login. |
| D10 | 15/11/2026 | minSdk elevado de 24 para **26** para usar `java.time` (datas/horários) sem desugaring. |
| D11 | 15/11/2026 | Não há tela "Dashboard" separada: como no protótipo, a tela **Tarefas** é a inicial e contém o resumo do dia e o progresso. |
| D12 | 15/11/2026 | **Login e cadastro** (pedido da equipe): contas locais no aparelho, cada tarefa/categoria pertence a um usuário. Como isso exige o banco, as Etapas 3 e 4 foram executadas juntas. Substitui a D9 (perfil agora tem nome + e-mail, nome editável). |
| D13 | 15/11/2026 | Senhas guardadas apenas como hash **PBKDF2-HMAC-SHA256** (120.000 iterações, salt aleatório de 16 bytes). Sessão = id do usuário no DataStore. Não há servidor: contas não sincronizam entre aparelhos. |
| D14 | 15/11/2026 | **Tema black** (fundo #000000) como padrão; interruptor "Tema black" em Ajustes alterna para o tema claro do protótipo. Preferência salva no DataStore. |
| D15 | 15/11/2026 | "Urgência automática" desligada → novas tarefas começam com selo manual "Tranquilo". Tarefa pendente vencida sempre aparece como Atrasada. |
| D16 | 15/11/2026 | Excluir categoria não exclui tarefas: elas ficam "Sem categoria" (FK `SET_NULL`). Nome de categoria único por usuário (sem diferenciar maiúsculas). |
| D6 | 15/11/2026 | API externa aprovada: **BrasilAPI — Feriados nacionais**, consumida com Ktor + corrotinas, exibida no calendário. Gratuita, sem chave/credencial. Implementação prevista na Etapa 5 (com tratamento de falha de rede: o app funciona normalmente sem ela). |

### Observação sobre D4 × D6

A proposta do Trabalho Final exige "Consumo de API externa com Ktor e corrotinas". A BrasilAPI (`https://brasilapi.com.br/api/feriados/v1/{ano}`) atende ao requisito sem custo e sem credenciais, respeitando a D4.

## Etapa 0 — Análise e planejamento

- [x] Analisar o documento mestre.
- [x] Analisar o protótipo do Figma Make (11 capturas em `Instruções/IMG`).
- [x] Identificar telas e fluxos: Tarefas (inicial, com resumo do dia e filtros Todas/Urgentes/Hoje/Concluídas), Nova/Editar tarefa, Detalhes, Calendário, Estatísticas, Ajustes (+ Categorias).
- [x] Definir a arquitetura inicial (D3).
- [x] Definir o escopo do MVP (seção 4.1 do documento mestre, sem integrações — D4).
- [x] Registrar dúvidas e dependências (protótipo visual e API externa).

## Etapa 1 — Configuração do projeto ✅

- [x] Criar o projeto Android (Kotlin, Jetpack Compose, Gradle Kotlin DSL, version catalog).
- [x] Configurar tema inicial Material 3 (`ui/theme`) — paleta **provisória**, será ajustada ao protótipo.
- [x] Organizar estrutura de pacotes (apenas o que existe: `MainActivity.kt` e `ui/theme`).
- [x] Confirmar que compila (`assembleDebug`), testes unitários passam (`testDebugUnitTest`) e o app abre no emulador Pixel_8.

## Etapa 2 — Estrutura visual e navegação ✅

- [x] Navegação principal: `NavHost` + Bottom Navigation (Tarefas, Calendário, Estatísticas, Ajustes) e rotas internas (nova tarefa, detalhes, edição).
- [x] Componentes reutilizáveis (`ui/components`): `AppCard`, `ScreenHeader`, `IconTile`, `InfoRow`, `EmptyState`, `UrgencyBadge`, `CategoryChip`, `Pill`, `TaskCard`.
- [x] Tela Tarefas: cabeçalho com data, saudação e progresso do dia; busca; filtros com contador de urgentes; lista ordenada por prioridade; estado vazio; botão +.
- [x] Telas Detalhes, Nova/Editar tarefa (data rápida, seletores de data/hora, selo automático/manual, categoria, subtarefas), Calendário (faixa semanal), Estatísticas (calculadas a partir dos dados) e Ajustes.
- [x] Tema ajustado às cores do protótipo (claro e escuro).
- [x] Conferência visual com a referência no emulador (Pixel_8).

**Ainda não funcionam (próximas etapas):** salvar tarefa, concluir/reabrir, excluir, marcar subtarefa, gerenciar categorias (Etapa 3); persistência, nome do usuário e preferências salvas (Etapa 4); sino de lembretes e feriados (Etapa 5).
Os dados exibidos vêm de `InMemoryTaskRepository` (**temporários**, recriados a cada abertura).

Regras de urgência automática: prazo vencido → Atrasada; hoje ou amanhã → Urgente; até 3 dias → Atenção; depois → Tranquilo. Selo manual pode ser escolhido no formulário; tarefa pendente com prazo vencido sempre aparece como Atrasada.

Diferença intencional em relação ao protótipo: o item "Cor principal" de Ajustes não foi incluído (fora do escopo do documento mestre); a área "Sobre" com os criadores foi adicionada (exigida pelo documento mestre).

## Etapas 3 e 4 — Funcionalidades de tarefas + Persistência local (Room) ✅

**Tarefas**
- [x] Criar e editar (título obrigatório, máx. 80 caracteres; descrição; data/hora; selo; categoria; subtarefas).
- [x] Excluir com confirmação (botão na tela de detalhes e gesto de arrastar o card para a esquerda; ação acessível para TalkBack).
- [x] Concluir e reabrir (checkbox no card e botão nos detalhes); marcar subtarefas.
- [x] Prioridade (selo automático/manual), categoria e prazo; pesquisa e filtros.

**Categorias:** criar, editar (nome + 8 cores), excluir com confirmação; 4 padrão criadas para cada nova conta.

**Usuários:** tela única de login/cadastro com validação (nome, e-mail, senha ≥ 6, confirmação), mostrar/ocultar senha, mensagem genérica "E-mail ou senha incorretos", e-mail duplicado recusado; editar nome e sair da conta (com confirmação) em Ajustes.

**Persistência**
- [x] Room: `users`, `categories`, `tasks`, `subtasks` com chaves estrangeiras (CASCADE/SET_NULL) e índice único de e-mail. Esquema exportado em `app/schemas/`.
- [x] Todas as consultas filtram por `userId` — um usuário nunca acessa dados de outro.
- [x] DataStore: sessão, tema, urgência automática.
- [x] Erros de leitura/gravação tratados: operações em `launchSafely` com mensagem no Snackbar; DataStore corrompido volta aos padrões.
- [x] Os dados temporários da Etapa 2 (`InMemoryTaskRepository`) foram removidos.

**Arquitetura adicionada:** `data/local` (entidades, DAOs, banco, conversores), `data/preferences`, `data/security`, `data/repository` (`AuthRepository`, `RoomTaskRepository`), `domain/AuthValidator`, `ui/BaseViewModel` + Snackbar global, `ui/screens/auth`, `ui/screens/categories`.

### Testes realizados (15/11/2026)

Unitários (`testDebugUnitTest`): **9 de 9 passando** — regras de urgência (4) e autenticação/validação/hash (5).

Manuais no emulador Pixel_8 (instalação limpa):
| Cenário | Resultado |
|---|---|
| Entrar com campos vazios / senha curta / senhas diferentes | Mensagens de validação exibidas ✅ |
| Cadastro → entra direto, lista vazia com instrução | ✅ |
| Criar tarefa, editar título/categoria/subtarefas | ✅ |
| Marcar subtarefa, concluir e reabrir tarefa | ✅ |
| Fechar o app (force-stop) e abrir: sessão e tarefas mantidas | ✅ |
| Atualizar o APK mantendo dados | ✅ |
| Arrastar para excluir: Cancelar mantém, Excluir remove + aviso | ✅ |
| Categoria com nome duplicado ("estudos" × "Estudos") recusada | ✅ |
| Excluir categoria com tarefa: tarefa fica sem categoria | ✅ |
| Sair da conta (confirmação) → tela de login | ✅ |
| Login com senha errada → "E-mail ou senha incorretos" | ✅ |
| Segundo usuário: não vê tarefas nem categorias do primeiro | ✅ |
| Cadastro com e-mail já usado (maiúsculas) recusado | ✅ |
| Voltar ao primeiro usuário: vê apenas as próprias tarefas | ✅ |
| Alternar tema black/claro e reiniciar: preferência mantida | ✅ |
| Crashes no logcat durante os testes | 0 ✅ |

Problemas encontrados e corrigidos na revisão: subtarefa e interruptores só respondiam ao toque na caixinha (linha inteira agora é tocável); saudação usava nome completo (agora primeiro nome); ícones da barra de status escuros no tema black.

### Ambiente e versões utilizadas

| Item | Versão |
|---|---|
| Android Gradle Plugin | 9.3.3 (Kotlin embutido) |
| Kotlin / plugin Compose | 2.2.10 |
| Gradle | 9.5.0 |
| Compose BOM | 2026.02.01 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
| Navigation Compose | 2.10.2 |
| Lifecycle (ViewModel/Compose) | 2.11.0 |
| Material Icons Extended | 1.7.8 |
| Room | 2.8.5 (KSP 2.3.14) |
| DataStore Preferences | 1.2.1 |
| JDK do build | JBR 25 do Android Studio |

Se o build falhar em outra máquina por versão, usar como alternativa as versões do arquivo `versios_libs_fails.txt` (AGP 8.13.2, Kotlin 2.0.21, Compose BOM 2024.09.00 etc.).

## Problemas encontrados

| Data | Problema | Causa | Solução |
|---|---|---|---|
| 15/11/2026 | Build recusado pelo AGP: "project path contains non-ASCII characters". | Pasta do OneDrive contém acentos (`Instituição`, `Programação`). | `android.overridePathCheck=true` em `gradle.properties`. |
| 15/11/2026 | Telas internas sobrepostas à barra de status; botões e interruptores com lilás padrão do M3; vão nas barras de progresso. | Insets não aplicados no cabeçalho; cores `secondaryContainer`/`surfaceContainerHighest` não definidas no tema; `gapSize` padrão. | Corrigidos na revisão da Etapa 2 (insets no `ScreenHeader`, cores no tema, `gapSize = 0.dp`). |
| 15/11/2026 | Testes unitários falhavam com `ClassNotFoundException`. | Caminho não-ASCII no classpath do processo de testes no Windows (confirmado: em caminho sem acentos os testes passam). | `build.gradle.kts` (raiz) redireciona a pasta `build` para `%USERPROFILE%\.todolist-build` quando o caminho tem acentos. O APK de debug fica em `%USERPROFILE%\.todolist-build\_app\outputs\apk\debug\`. |

## Como validar

1. Abrir a pasta `Projeto ToDo` no Android Studio (File › Open).
2. Aguardar o Gradle Sync e executar o app no emulador.
3. Pelo terminal: `gradlew assembleDebug testDebugUnitTest` (com `JAVA_HOME` apontando para o JBR do Android Studio).

## Histórico de alterações

| Data | Alteração |
|---|---|
| 15/11/2026 | Etapa 0: respostas da equipe registradas (D1–D5). |
| 15/11/2026 | D6: BrasilAPI (feriados) aprovada como API externa. Tentativa de acesso ao protótipo no Figma Make sem sucesso (requer login). |
| 15/11/2026 | Etapa 1: projeto Android criado (`com.todolist.app`), tema provisório, correções de caminho com acentos, build e testes OK, app aberto no emulador. |
| 15/11/2026 | Decisões D7–D11. Etapa 2: navegação, componentes e todas as telas principais com dados temporários; revisão visual no emulador e correções. Arquivos de instrução movidos para `Instruções/`. |
| 15/11/2026 | D12–D16. Etapas 3 e 4: login/cadastro local, Room (usuários, categorias, tarefas, subtarefas), DataStore, CRUD de tarefas e categorias, tema black, testes unitários e manuais. |
