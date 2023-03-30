Neste projeto desenvolvemos um motor de pesquisa, semelhante ao do Google.
Este motor tem as seguintes funcionalidades:
- Indexar um URL novo, sendo que o motor trata de indexar os URLs que encontrar dentro deste.
- Pesquisar páginas que contenham um conjunto de termos, ordenando-as por relevância (número de páginas que referenciam esta página)
- Consultar as páginas que têm ligação para uma página específica
- Página de administração, com informações importantes sobre os diversos componentes.

# Arquitetura
Para implementar estas funcionalidades usamos uma arquitetura distribuída com:
- Search Module
- Múltiplos Downloaders
- Múltiplos Storage Barrels
- Queue de URLs a indexar.

## Downloaders
Esta componente tem o objetivo de obter as páginas Web, dado um URL. Este URL é obtido da **Queue de URLs**, via **TCP**, usando a Porta A (8080).
Após a receção do URL, o Downloader obtém o conteúdo da página.

Conteúdo da página:
- URL
- Título
- Palavras
- URL's para outras páginas
Tanto para o título quanto para as palavras, são removidos os caracteres "|", ";" e "\n".
Páginas que não contenham título são consideradas inválidas, logo não são indexadas.

Após a obtenção do conteúdo, este é enviado para o **Storage Barrel**, usando MULTICAST com o endereço 224.3.2.1 e com a porta 4321.
É também enviado para a **URL Queue** usando **TCP** os URLs que foram encontrados na página, usando a Porta B (8081).

Podem existir vários Downloaders a correr em paralelo, visto que são Threads. Cada um recebe um URL da **URL Queue** e processa-o, independentemente dos outros.


## Index Storage Barrel
É a componente que guarda toda a informação indexada. Recebe os contéudos das páginas Web, processados pelos Downloaders, via **MULTICAST** com o endereço 224.3.2.1 e a porta 4321.

Os Storage Barrels são Threads identificadas por um ID, sendo que cada um é independente dos outros.
Os Storage Barrels com ID par guardam as palavras que começam por letras de N a Z, enquanto que os Storage Barrels com ID ímpar guardam todas as outras palavras ou números.
Ao receber os dados de uma página, os Barrels fazem o parsing do contéudo, obtendo assim as palavras, URLs referenciados e título da página.
Após o parsing desta informação o Storage Barrel guarda esta informação em memória, em dois indexs invertidos:
- Index de palavras: Guarda na chave a palavra e no valor uma lista de URLs que contêm esta palavra.
- Index de links: Guarda na chave o URL e no valor os URLs que referenciam este, o título da página, e uma breve descrição da mesma. Esta descrição é composta pelas 15 primeiras palavras encontradas na página, que não sejam o título.

Esta informação é também guardada em ficheiros de texto, um para cada Index.

Os Storage Barrels possuem também funções de pesquisa, que são usadas pelo Search Module usando RMI, onde o nome do objeto remoto é o seguinte: "rmi://localhost/Barrel**ID**", onde ID é o ID do Storage Barrel.
As chamadas de RMI são as seguintes:
- Pesquisar termos: Os Storage Barrels vão retornar uma lista de URLs e informações sobre os mesmos (título e descrição). Esta lista de URLs é composta apenas pelos URLs que contenham todos os termos pesquisados e é ordenada pelo número de URLs que referenciam o URL.
- Pesquisar por páginas que referenciam um URL: Os Storage Barrels vão retornar uma lista de URLs. Esta lista de URLs é composta apenas pelos URLs que referenciam o URL pesquisado.


## Search Module
Esta componente é a componente que comunica com os **Storage Barrels** usando **RMI**.

Uma das chamadas de RMI tem como objetivo obter os URLs que contêm um conjunto de palavras. Para isto o Search Module chama a função de pesquisa de termos dos Storage Barrels. Como os Storage Barrels estão **particionados**, o Search Module chama a função de pesquisa em 2 Storage Barrels aleatórios, um com ID par e outro com ID impar. O Search Module faz uma intersecção das listas de URLs, para obter apenas os URLs que contêm todos os termos pesquisados. É também adicinada a pesquisa a um dicionário que contém as pesquisas mais frequentes. O Search Module retorna a lista de URLs ordenada pelo número de URLs que referenciam o URL.

Outra chamada de RMI tem como objetivo obter os URLs que referenciam um URL. O Search Module chama a função de pesquisa de URLs em Storage Barrels aleatórios. É depois retornada uma lista com os URLs que referenciam o URL pesquisado.

Existe também uma chamada de RMI para indexar URLs. O Search Module comunica com a **URL Queue** via **TCP** usando a Porta B (8081), enviando o URL a indexar.

Outra chamada de RMI serve para verificar as credenciais de login. O Search Module retorna o valor booleano da verificação que vem da **Admin Page**.

Todas os argumentos das pesquisas são obtidos através do cliente por RMI também.
Para isto o nome do objeto remoto criado é o seguinte: *"SearchModule"*.

Na nossa implementação, o Search Module é também aquele responsável por iniciar as Threads dos Storage Barrels e dos Downloaders e também criar a Admin Page.

### Admin Page
A Admin Page é um objeto criado no Search Module.
Esta está responsável por obter através de Multicast as informações dos Storage Barrels e dos Downloaders, em tempo real, ligando-se ao mesmo Porto e IP usados ao longo do projeto.
Estas informações são:
- Downloaders: Estado: "Active" se estiver a fazer download de uma página, "Waiting" se estiver à espera de receber um URL e "Offline" se tiver crashado ou parado de correr. Aparece também a página que este está a descarregar, ou a última página que descarregou no caso de já estar offline.
- Barrels: Estado: "Waiting" se estiver à espera de receber um URL, "Active" se estiver a armazenar informação e "Offline" se tiver crashado ou parado de correr. Aparece também o IP e Porto dos Storage Barrels.
- Termos mais pesquisados: Um top 10 composto pelos termos que foram mais pesquisados, bem como o número de pesquisas que cada um teve.


## RMI Client
Este é o cliente que comunica com o Search Module, usando RMI. Este cliente tem as seguintes funcionalidades:
- Indexar um URL: Para isto o cliente introduz o URL a indexar e o Search Module é chamado para indexar este URL.
- Pesquisar por um conjunto de termos: Para isto o cliente introduz os termos a pesquisar e o Search Module é chamado para pesquisar por estes termos. Os URLs são retornados em páginas de 10 em 10.
- Pesquisar por URLs que referenciam um URL: Para isto o cliente precisa de efetuar login recorrendo mais uma vez ao Search Module. É mostrado ao cliente uma lista de URLs que referenciam o URL pesquisado.
- Mostrar a pagina de adminstração: Esta página contém várias informações relativas aos vários sistemas.
- Realizar o login.


## URL Queue
Esta componente é a componente que guarda os URLs a indexar. É uma fila de URLs, onde os URLs são adicionados pelo Search Module e retirados pelos Downloaders.
A Queue possui um histórico de URLs indexados, para que não sejam indexados URLs repetidos.
A URL Queue possui duas Threads:
- Uma Thread que recebe os URLs a indexar, via **TCP** usando a Porta B (8081). Estes URLs são adicionados à fila. Se o URL começar por "[RESEND]https://", então o URL é adicionado à fila ignorando o histórico. (Este URL é enviado quando algum Downloader falha a processar um URL).
- Uma Thread que envia os URLs a indexar, via **TCP** usando a Porta A (8080). Estes URLs são retirados da fila e enviados para os Downloaders.



# Prótocolo Multicast
O protucolo multicast é usado para a comunicação entre os Storage Barrels e os Downloaders e entre os Downloaders/Storage Barrels para a Admin Page.
O protocolo é o seguinte:
    - O Protocolo usado para a comunicação entre os Storage Barrels e os Downloaders é o seguinte:
        type | url; item_count | number; url | www.example.com; referenced_urls | url1 url2 url3; title | title; words | word1 word2 word3 
    - O Protocolo usado para a comunicação entre os Downloaders/Storage Barrels para a Admin Page é o seguinte:
        type | Downloader; index | index_number; status | Active; url | www.example.com;
        type | Barrel; index | 0; status | Active; ip | ip_example; port | 1234;

Em qualquer sitio onde seja recebida mensagens por multicast, é feito o parsing da mesma de forma a obter os dados desejados. Por exemplo, no caso do Downloader, é feito o parsing da mensagem para obter o index do Downloader, o seu estado e o URL que está a processar. No caso do Storage Barrel, é feito o parsing da mensagem para obter o index do Storage Barrel, o seu estado, o IP e o Porto.


# Tratamento de Erros
Tentámos ao máximo fazer com que cada componente fosse independente dos outros, e que cada componente fosse capaz de lidar com os erros que possam ocorrer.
Para isto, por exemplo nos Downloaders, caso não consiga ligar-se à Queue de URLs para ir buscar um URL (por exemplo se a Queue tiver offline), ele espera 3 segundos e volta a tentar de novo até conseguir. O mesmo acontece quando vai enviar o URL para a Queue. Esta implementação é aplicada a todos os componenentes que se comunicam por TCP.

Todas as exceções que encontramos são tratadas e maior parte delas são exibidas através de mensagens de erro que especificam bem a causa e sem nunca chegarem ao cliente, sem que os componenetes deixem de funcionar. Um exemplo é no Downloader quando usamos o Jsoup para se conectar a um URL, este pode lançar uma exceção caso não consiga fazer a conexão. Neste caso o URL e enviado de volta para a Queue para ser processado mais tarde.

Com estes tratamentos de exceções, conseguimos que o sistema seja resiliente a falhas, podendo correr uns componenetes sem os outros, ficando à espera que estes voltem a estar ativos. Através dos nossos testes, o cliente é incapaz de crashar o sistema, e mesmo que um componenente falhe, o cliente não é afetado.

Para demonstrar isto criamos situações de anomalia propositada nos Storage Barrels e nos Downloaders (que podem ser ativadas ou desativadas através de uma flag no ficheiro de configurações). Por exemplo nos Barrels, há a probabilidade de um falhar quando o cliente faz uma busca de termos, sendo que se isto acontecer, o Search Module encarrega-se de fazer a pesquisa em outro Barrel.
Nos Downloaders, quando um falha enquanto processa o URL, o URL é enviado de volta para a Queue para ser processado mais tarde por outro Downloader.