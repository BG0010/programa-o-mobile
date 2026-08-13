

## Parte 1 — Dez exercícios

### Exercício 1 · val e var

#### Código Corrigido
```kotlin
fun main() {
    val disciplina = "Programação Mobile"
    var aulas = 20
    aulas = 21
    println("$disciplina tem $aulas aulas")
}
```

#### Explicação do Erro
O erro `Val cannot be reassigned` ocorre porque a variável `aulas` foi declarada originalmente como imutável (`val`), o que impede a reatribuição de seu valor após a inicialização.

---

### Exercício 2 · val e var

* `val nomeDoCurso = "Análise e Desenvolvimento de Sistemas"`
  * **Justificativa:** O nome do curso é uma informação constante e fixa durante a execução do programa.
* `var totalDeAlunos = 0`
  * **Justificativa:** A quantidade de alunos é um valor incremental que sofre reatribuição ao longo do tempo.
* `val anoLetivo = 2026`
  * **Justificativa:** O ano letivo permanece inalterado durante o ciclo de execução.
* `var notaFinal = 0.0`
  * **Justificativa:** A nota é recalculada e atualizada a cada avaliação, exigindo mutabilidade.

---

### Exercício 3 · tipos e inferência

#### Tipos Inferidos
* `a`: `Int`
* `b`: `Double`
* `c`: `String`
* `d`: `Char`
* `e`: `Boolean`
* `f`: `Long`

#### Previsão e Verificação
* *Previsão do aluno:* `Int`, `Double`, `String`, `Char`, `Boolean`, `Long`
* *Erros na previsão:* Nenhum.

---

### Exercício 4 · null safety

#### Exceção Encontrada
`java.lang.NullPointerException` (ou `KotlinNullPointerException`).

#### Código Reescrito
```kotlin
fun main() {
    val apelido: String? = null
    println(apelido?.uppercase() ?: "SEM APELIDO")
}
```

---

### Exercício 5 · null safety

```kotlin
fun tamanhoSeguro(texto: String?): Int = texto?.length ?: 0

fun main() {
    println(tamanhoSeguro("Kotlin"))   // imprime 6
    println(tamanhoSeguro(null))       // imprime 0
}
```

---

### Exercício 6 · null safety

#### Código Completo
```kotlin
data class Endereco(val cidade: String?)
data class Aluno(val nome: String, val endereco: Endereco?)

fun main() {
    val ana = Aluno("Ana", null)
    val beto = Aluno("Beto", Endereco("São Paulo"))
    val caio = Aluno("Caio", Endereco(null))

    val alunos = listOf(ana, beto, caio)
    for (aluno in alunos) {
        val cidade = aluno.endereco?.cidade ?: "não informada"
        println("${aluno.nome}: $cidade")
    }
}
```

#### Saída do Console
```text
Ana: não informada
Beto: São Paulo
Caio: não informada
```

---

### Exercício 7 · funções

#### Código Completo
```kotlin
fun saudacao(nome: String, prefixo: String = "Olá"): String = "$prefixo, $nome!"

fun main() {
    // 1) Usando o valor padrão
    println(saudacao("Maria"))        // Saída: Olá, Maria!

    // 2) Passando outro prefixo
    println(saudacao("Maria", "Bem-vinda")) // Saída: Bem-vinda, Maria!
}
```

---

### Exercício 8 · funções

#### Chamadas e Saídas
```kotlin
fun matricular(nome: String, curso: String = "ADS", periodo: Int = 1) {
    println("$nome — $curso — ${periodo}º período")
}

fun main() {
    // 1) Aproveitando os dois valores padrão
    matricular("Lucas")
    // Saída: Lucas — ADS — 1º período

    // 2) Mudando apenas o período (usando argumento nomeado)
    matricular("Lucas", periodo = 3)
    // Saída: Lucas — ADS — 3º período

    // 3) Passando argumentos fora da ordem de declaração
    matricular(periodo = 2, curso = "Engenharia", nome = "Lucas")
    // Saída: Lucas — Engenharia — 2º período
}
```

---

### Exercício 9 · lambdas

#### Código Completo
```kotlin
fun main() {
    val nomes = listOf("Ana", "Beatriz", "Caio", "Daniela")

    // 1) Declaração da lambda com tipo explícito
    val emMaiusculas: (String) -> String = { texto -> texto.uppercase() }

    // 2) Versão com parâmetro nomeado
    val versaoNomeada = nomes.filter { nome -> nome.length > 4 }.map { nome -> emMaiusculas(nome) }

    // 3) Versão com it
    val versaoIt = nomes.filter { it.length > 4 }.map(emMaiusculas)

    println(versaoNomeada)
    println(versaoIt)
}
```

#### Saída Impressa
```text
[BEATRIZ, DANIELA]
[BEATRIZ, DANIELA]
```

---

### Exercício 10 · lambdas · ponte para o Compose

#### Código Completo
```kotlin
fun botao(rotulo: String, onClick: () -> Unit) {
    println("[$rotulo] clicado")
    onClick()
}

fun main() {
    // Chamada usando trailing lambda
    botao("Salvar") {
        println("Ação executada com sucesso!")
    }
}
```

#### Saída do Console
```text
[Salvar] clicado
Ação executada com sucesso!
```

---

## Parte 2 — Exercício de leitura

### Trecho A
* **Previsão:**
  ```text
  3
  -1
  4
  ```
* **Saída Real:**
  ```text
  3
  -1
  4
  ```

---

### Trecho B
* **Previsão:**
  ```text
  [20, 40]
  ```
* **Saída Real:**
  ```text
  [20, 40]
  ```

---

### Trecho C
* **Previsão:**
  ```text
  10
  15
  20
  ```
* **Saída Real:**
  ```text
  10
  15
  20
  ```