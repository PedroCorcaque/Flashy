# Flashy 💜

Aplicativo Android de **flashcards** para estudar do seu jeito: organize os cards em tópicos e subtópicos, estude com cronômetro, veja seu resultado no final e refaça só o que errou.

O Flashy nasceu como um presente: um app feito com carinho para quem está estudando e merece uma ferramenta bonita e sem complicação.

## Funcionalidades

- **Tópicos** com nome e cor personalizados (ex.: Física, Química, História).
- **Subtópicos** dentro de cada tópico (ex.: Física › Cinemática), com filtro por chips na tela do tópico.
- **Cards** com frente e verso, criados e editados em uma tela só, com escolha de tópico e subtópico.
- **Modo de estudo** com virada de card animada e botões *Acertei* / *Errei*.
- **Inverter cartas**: estude começando pelo verso.
- **Cronômetro** na tela de estudo, que para quando a sessão termina.
- **Resultado final** com porcentagem, acertos, erros e tempo.
- **Refazer só as que errou**, quantas vezes quiser, até zerar os erros.
- **Estudar tudo de uma vez** pelo botão *Estudar* da tela inicial, escolhendo "Todos os tópicos" ou um tópico específico.
- **Editar e excluir** tópicos, subtópicos e cards, sempre com confirmação antes de apagar.
- **Tudo offline**: os dados ficam salvos no próprio aparelho, sem conta e sem internet.

## Tecnologias

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) com [Material 3](https://m3.material.io/)
- [Room](https://developer.android.com/training/data-storage/room) para o banco de dados local
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- `ViewModel`, Kotlin Coroutines e `Flow` / `StateFlow`

## Arquitetura

O projeto segue uma arquitetura em camadas simples (MVVM), sem framework de injeção de dependência:

```
UI (Compose)  →  ViewModel  →  Repository  →  DAO (Room)
```

Os arquivos ficam em `app/src/main/java/com/pedrocorcaque/flashy/`:

```
├── FlashyApplication.kt        # cria o banco e o repositório
├── MainActivity.kt
├── data/
│   ├── model/                  # entidades: Topic, Subtopic, Flashcard
│   ├── local/                  # FlashyDatabase (com migrations) e FlashyDao
│   └── repository/             # FlashyRepository
└── ui/
    ├── navigation/             # rotas e NavGraph
    ├── theme/                  # cores, tipografia e tema escuro
    ├── components/             # componentes reutilizáveis (ex.: diálogo de exclusão)
    ├── util/                   # utilitários (ex.: formatação do cronômetro)
    └── screens/
        ├── home/               # lista de tópicos e seletor de estudo
        ├── topic/              # cards e subtópicos de um tópico
        ├── flashcard/          # sessão de estudo e tela de resultado
        ├── addedit/            # criar/editar tópico e card
        └── settings/           # configurações
```

### Modelo de dados

| Entidade    | Campos principais                                         |
| ----------- | --------------------------------------------------------- |
| `Topic`     | `id`, `name`, `cardCount`, `colorArgb`, `iconName`        |
| `Subtopic`  | `id`, `topicId`, `name`                                   |
| `Flashcard` | `id`, `topicId`, `subtopicId` (opcional), `front`, `back` |

- Excluir um **tópico** apaga também seus cards e subtópicos.
- Excluir um **subtópico** mantém os cards, que apenas ficam sem subtópico.

## Como executar

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) em uma versão recente
- Um aparelho Android ou um emulador (minSdk = 24)

### Passos

```bash
git clone https://github.com/PedroCorcaque/flashy.git
cd flashy
```

1. Abra a pasta no Android Studio e aguarde o Gradle sincronizar.
2. Escolha um emulador ou conecte um aparelho com a depuração USB ativa.
3. Clique em **Run ▶** (ou `Shift + F10`).

Para gerar um APK de depuração pelo terminal:

```bash
./gradlew assembleDebug
```

O arquivo sai em `app/build/outputs/apk/debug/`.

## Como usar

1. Na tela inicial, toque em **+** para criar um tópico.
2. Abra o tópico, crie subtópicos com **+ Subtópico** e adicione cards com **Adicionar card**.
3. Toque em **Estudar** (dentro do tópico ou na barra inferior), vire o card e marque *Acertei* ou *Errei*.
4. No final, veja o resultado e use **Refazer as que errei** para revisar só os erros.

## Limitações conhecidas

Algumas telas ainda são apenas visuais e não fazem nada:

- Itens de **Configurações** (Aparência, Notificações, Backup local, Sobre).
- Aba **Perfil** da barra inferior.
- Seleção de ícone ou imagem ao criar um tópico.

## Roadmap

- [ ] Backup local (exportar e importar)
- [ ] Lembretes de estudo (notificações)
- [ ] Tema claro
- [ ] Escolha de ícone para o tópico
- [ ] Testes automatizados
- [ ] Textos em `strings.xml` e suporte a outros idiomas

## Contribuindo

Contribuições são bem-vindas! Abra uma *issue* para discutir a ideia ou envie um *pull request*. Para mudanças maiores, é melhor conversar antes.

1. Faça um *fork* do projeto
2. Crie uma branch: `git checkout -b minha-feature`
3. Faça o commit: `git commit -m "Adiciona minha feature"`
4. Envie: `git push origin minha-feature`
5. Abra um *pull request*

## Licença

Distribuído sob a licença **<!-- MIT -->**. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.

---

Feito com 💜
