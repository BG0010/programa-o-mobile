# Diário de Uso de Inteligência Artificial
**Aluno:** [Seu Nome Completo]  
**Disciplina:** Programação Mobile  
**Professor:** Msc. Marnes Adão Simão Cassule  
**Instituição:** UNASP-SP  
**Data:** 12/08/2026  

---

## Parte 1 — Dez exercícios

### Exercício 1 · val e var
* **Uso de IA:** Não usei.
* **Reflexão / Desafios:** Identifiquei diretamente que a reatribuição da variável `aulas` falhou porque ela foi declarada como imutável (`val`). Corrigi para `var`.

---

### Exercício 2 · val e var
* **Prompt enviado para a IA:** "Decida val ou var para cada declaração e escreva uma justificativa curta para cada escolha: nomeDoCurso, totalDeAlunos, anoLetivo, notaFinal."
* **O que a IA respondeu:** Recomendou `val` para `nomeDoCurso` e `anoLetivo` por serem constantes, e `var` para `totalDeAlunos` e `notaFinal` por sofrerem mutação/recalculo.
* **O que aprendi / O que tive que corrigir:** Entendi a regra de negócios por trás do uso de variáveis mutáveis e imutáveis em Kotlin, confirmando o critério da aula de iniciar sempre com `val`.

---

### Exercício 3 · tipos e inferência
* **Prompt enviado para a IA:** "Quais são os tipos inferidos em Kotlin para: 10, 10.0, '10', '1', true, 10L?"
* **O que a IA respondeu:** `Int`, `Double`, `String`, `Char`, `Boolean` e `Long`.
* **O que aprendi / O que tive que corrigir:** Compreendi a diferença de inferência de tipos entre caracteres simples (`Char`), textos (`String`) e literais numéricos com sufixo (`Long`).

---

### Exercício 4 · null safety
* **Prompt enviado para a IA:** "Como reescrever um código Kotlin que usa '!!' para evitar NullPointerException e imprimir 'SEM APELIDO' quando for nulo?"
* **O que a IA respondeu:** Sugeriu o uso da navegação segura (`?.`) combinada com o operador Elvis (`?:`).
* **O que aprendi / O que tive que corrigir:** Aprendi a lidar com valores nulos de forma segura e idiomática em Kotlin sem forçar a unboxing com `!!`.

---

### Exercício 5 · null safety
* **Prompt enviado para a IA:** "Como fazer uma função em linha única em Kotlin que retorne o tamanho de uma String? ou 0 se for nula?"
* **O que a IA respondeu:** `fun tamanhoSeguro(texto: String?): Int = texto?.length ?: 0`
* **O que aprendi / O que tive que corrigir:** Entendi a sintaxe de corpo de expressão (`=`) para funções simples e o encadeamento do operador Elvis.

---

### Exercício 6 · null safety
* **Prompt enviado para a IA:** "Como acessar com segurança uma propriedade aninhada nula em Kotlin e colocar um fallback padrão?"
* **O que a IA respondeu:** Utilizou a encadeação `aluno.endereco?.cidade ?: "não informada"`.
* **O que aprendi / O que tive que corrigir:** Notei como o operador de navegação segura encadeia chamadas nulas em múltiplos níveis de forma limpa.

---

### Exercício 7 · funções
* **Prompt enviado para a IA:** "Como transformar uma função Kotlin em corpo de expressão única com parâmetro padrão?"
* **O que a IA respondeu:** Mostrou a assinatura `fun saudacao(nome: String, prefixo: String = "Olá"): String = "$prefixo, $nome!"`.
* **O que aprendi / O que tive que corrigir:** Percebi a utilidade de definir parâmetros default para evitar sobrecarga manual de métodos.

---

### Exercício 8 · funções
* **Prompt enviado para a IA:** "Como chamar uma função em Kotlin alterando apenas o último parâmetro ou passando fora da ordem?"
* **O que a IA respondeu:** Demonstrou o uso de argumentos nomeados (ex: `matricular(periodo = 3)` e `matricular(periodo = 2, curso = "Engenharia", nome = "Lucas")`).
* **O que aprendi / O que tive que corrigir:** Aprendi como os parâmetros nomeados dão flexibilidade ao invocar funções com argumentos default.

---

### Exercício 9 · lambdas
* **Prompt enviado para a IA:** "Como declarar uma lambda explícita `(String) -> String` e usar `filter` e `map` com parâmetro nomeado e com `it` em Kotlin?"
* **O que a IA respondeu:** Forneceu as duas sintaxes (`{ nome -> ... }` e `{ it ... }`) além da declaração `val emMaiusculas: (String) -> String = { it.uppercase() }`.
* **O que aprendi / O que tive que corrigir:** Entendi a convenção da palavra-chave `it` para lambdas de parâmetro único.

---

### Exercício 10 · lambdas · ponte para o Compose
* **Prompt enviado para a IA:** "Como criar uma função que recebe uma função callback e chamá-la usando trailing lambda em Kotlin?"
* **O que a IA respondeu:** Definiu o parâmetro `onClick: () -> Unit` e explicou a sintaxe de passar o bloco fora dos parênteses da função.
* **O que aprendi / O que tive que corrigir:** Aprendi a sintaxe base utilizada no Jetpack Compose para componentes com eventos de clique.

---

## Parte 2 — Exercício de leitura

### Trecho A
* **Prompt enviado para a IA:** "O que o loop `for (n in nomes)` imprime em uma lista `listOf("Ana", null, "Beto")` executando `n?.length ?: -1`?"
* **O que a IA respondeu:** Explicou que imprime `3`, `-1` e `4`.
* **O que aprendi / O que tive que corrigir:** Confrontei minha previsão inicial sobre como o operador Elvis trata elementos nulos no percurso de coleções.

---

### Trecho B
* **Prompt enviado para a IA:** "Qual a saída de `listOf(1, 2, 3, 4, 5).filter { it % 2 == 0 }.map { it * 10 }`?"
* **O que a IA respondeu:** Explica que filtra os pares (`[2, 4]`) e multiplica por 10, gerando `[20, 40]`.
* **O que aprendi / O que tive que corrigir:** Validei a ordem de execução das operações encadeadas em coleções.

---

### Trecho C
* **Prompt enviado para a IA:** "Qual a saída de chamadas da função `fun dobrar(n: Int, vezes: Int = 2): Int = n * vezes`?"
* **O que a IA respondeu:** Retornou `10`, `15` e `20`.
* **O que aprendi / O que tive que corrigir:** Confirmei o funcionamento da substituição de argumentos por nomes na chamada das funções.
