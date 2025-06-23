# Projeto de Computação Paralela e Distribuída: Problema do Caixeiro Viajante
> Este repositório contém a implementação e análise de três soluções para o Problema do Caixeiro Viajante (TSP), como parte do trabalho prático da disciplina de *Sistemas Distribuídos*.

## Sobre o Problema: O Caixeiro Viajante (TSP)
O Problema do Caixeiro Viajante (do inglês, *Traveling Salesman Problem* - TSP) é um dos problemas de otimização mais famosos da ciência da computação. O desafio consiste em: dado um conjunto de cidades e as distâncias entre cada par delas, qual é a rota mais curta possível que visita cada cidade exatamente uma vez e retorna à cidade de origem?


## Descrição do Projeto
O objetivo deste trabalho é desenvolver, analisar e comparar o desempenho de três abordagens distintas para a resolução do TSP usando um algoritmo de força bruta:

1.  **Solução Sequencial:** Uma implementação padrão, de thread única, que serve como nossa base de comparação (*baseline*).
2.  **Solução Paralela:** Utiliza `Threads` em Java para dividir o trabalho de cálculo das rotas em múltiplos núcleos de um mesmo processador, visando acelerar a execução em uma única máquina.
3.  **Solução Distribuída:** Emprega `Java RMI` para distribuir o processamento entre várias máquinas em uma rede. Um nó "mestre" coordena a divisão do problema e delega subproblemas para nós "trabalhadores" (workers).

A análise de desempenho, incluindo a escalabilidade e eficiência das soluções, é apresentada no diretório *dados-coletados* e na apresentação de slides.

## Como Executar o Projeto
Certifique-se de ter o JDK (Java Development Kit) instalado e configurado em seu sistema.

### 1. Solução Sequencial - **DEPOIS A GENTE EDITA COM OS COMANDOS CERTOS, é só uma estrutura**
Navegue até o diretório da solução sequencial e execute o seguinte comando:

```bash
# Navegue para a pasta correta
cd ./codigo/sequencial

# Compile o arquivo .java
javac *.java

# Execute o programa
java ResolvedorPCV.java
```

### 2. Solução Paralela
Navegue até o diretório da solução paralela para compilar e executar:

```bash
# Navegue para a pasta correta
cd ./codigo/paralelo

# Compile o arquivo .java
javac *.java

# Execute o programa
java ResolvedorParalelo.java
```

### 3. Solução Distribuída (RMI)
A execução distribuída requer alguns passos para iniciar o registro RMI, os workers e o servidor.

**Passo A: Iniciar o Registro RMI**
Abra um terminal na raiz do diretório de código compilado e execute:

```bash
# Exemplo de caminho (ajuste conforme a estrutura da sua IDE)
cd ./codigo/distribuido/

# Inicie o serviço de registro RMI (deixe este terminal aberto)
rmiregistry
```

**Passo B: Iniciar os Workers**
Para cada nó trabalhador que você deseja usar, abra um novo terminal e execute:

```bash
# Navegue para a pasta correta
cd ./codigo/distribuido/

# Compile todos os arquivos (apenas na primeira vez)
javac *.java

# Inicie o Worker (repita em máquinas/terminais diferentes para mais workers)
java TSPWorker.java
```

**Passo C: Iniciar o Servidor (Master)**
Finalmente, em um novo terminal, inicie o servidor que irá gerenciar e distribuir o trabalho:

```bash
# Navegue para a pasta correta
cd ./codigo/distribuido/

# Execute o servidor para iniciar o processamento
java TSPServer.java
```

## Acesso Rápido
* ➡️ **[Código da Solução Sequencial](./codigo/sequencial/)**
* ➡️ **[Código da Solução Paralela](./codigo/paralelo/)**
* ➡️ **[Código da Solução Distribuída](./codigo/distribuido/)**
* ➡️ **[Dados Coletados dos Testes](./dados-coletados/)**
* ➡️ **[Apresentação de Slides](./apresentacao/)**

## Integrantes do Grupo
1. Carlos Hereman
2. Kauan Pedreira
3. Matheus Madureira
4. Luccas Maia
5. Thales Granja
6. Jeferson Rocha
7. Matheus Andrade