# Introdução
Neste projeto,  desenvolvemos uma página de pesquisas web, semelhante ao motor de busca Google.

# Funcionalidades
Este motor tem as seguintes funcionalidades:
- Indexar um URL novo, sendo que o motor trata de indexar os URLs que encontrar dentro deste.
- Pesquisar páginas que contenham um conjunto de termos, ordenando-as por relevância (número de páginas que referenciam esta página)
- Consultar as páginas que têm ligação para uma página específica
- Página de administração, com informações importantes sobre os diversos componentes.
- Indexar as stories de um utilizador do site de notícias Hacker News.
- Login/Logout e registo de utilizadores.
- Indexar as top stories do site de notícias Hacker News.

Todas as funcionalidades foram implementadas e são acessiveis através de uma interface web criada com springboot, html e css.

# Arquitetura

Na impletação da arquitetura foi seguido o modelo MVC, sendo o controlado o GoogolController e as views implementadas em html e css.
É importante notar que o model não é persistente, visto que essa resposabilidade é do projeto implementado na meta anteiror.

Para as funcionalidades do lado do servidor(meta anteiror), usamos uma arquiterura distribuida com :
- Search Module
- Múltiplos Downloaders
- Múltiplos Storage Barrels
- Queue de URLs a indexar.

Do lado do controlador, foi usado o springboot. Por outro lado, para as views foi usado html e css.

O websocket foi usado para obtermos atualizações em tempo real da página de administração, juntamente com ajax.

Para que as noticias do site de notícias Hacker News fossem indexadas, o nosso projeto foi integrado com a API do Hacker News.

# Controlador
O controlador é responsável por receber os pedidos do utilizador e encaminhá-los para o para o SerachModule, que por sua vez, devolve a informação necessária para as diferentes views.
Neste componente, foi implementado o websocket para obtermos atualizações em tempo real da página de administração, juntamente com ajax. Estas atualizações são feitas de 1 em 1 segundo, de modo a não sobrecarregar o servidor.

Na implementação do GoogolController, foram usados alguns paramentros em autowired, para que o springboot fizesse a injeção de dependências automaticamente.

# Endpoints

O seguintes endpoints foram implementados:
- / - Página inicial que tem um menu para todas as funcionalidades do motor de busca, bem como as opções de login, logout e registo.

- /login - Página de login, onde o utilizador pode fazer login com o seu username e password. No nosso caso, o login é feito lendo e verificando se o username e password introduzidos estão no ficheiro de texto onde estão guardados os utilizadores registados.
Após este, o utilizador tem acesso a todas as funcaionalidades do motor de busca.

- /logout - Faz logout do utilizador, redirecionando-o para a página inicial do menu.

- /register - Página de registo, onde o utilizador pode fazer registo com o seu username e password. No nosso caso, o registo é feito escrevendo para um ficheiro de texto.

- /indexNewUrl - Página onde o utilizador pode indexar um novo URL. Para isso, basta escrever o URL e clicar no botão "Indexar URL". Após isto, o url introduzido é enviado para o SearchModule, que trata de indexar o URL e os URLs que encontrar dentro deste.
No final, caso tenha corrido tudo bem, é apresentada uma mensagem de sucesso.

- /search - Página onde o utilizador pode pesquisar páginas que contenham um conjunto de termos, ordenando-as por relevância (número de páginas que referenciam esta página). Para isso, basta escrever os termos e clicar no botão "Pesquisar". Após isto, os termos introduzidos são enviados para o SearchModule, que trata de pesquisar as páginas que contêm esses termos e ordená-las por relevância.
Os resultados da pesquisa são aprensentados numa nova página, que os agrupa 10 a 10.

- /getSearchResults- Este endpoint é usado para obter os resultados da pesquisa, 10 a 10 como referido acima.

- /listPages - Página onde o utilizador pode consultar as páginas que têm ligação para uma página específica. Para isso, basta escrever o URL e clicar no botão "Consultar". Após isto, o URL introduzido é enviado para o SearchModule, que trata de consultar as páginas que têm ligação para esse URL. É importante notar que para aceder a esta página é necessário estar autenticado.

- /IndexHackersByUsername - Página onde o utilizador pode escrever um username do site de notcias tech Hacker News e clicar no botão, serão indexadas as stories desse utilizador. Para que isto aconteça o nosso projeto foi integrado com a API do Hacker News. Apos todas as informações terem cehgado ao controlador, este mostra-as ao utilizador e envia-as para o SearchModule.

- /IndexHackerNews - Endpoint que é usado para indexar as top stories do site de notícias Hacker News, através da API do Hacker News.  Isto acontece porque é feito um request com o método GET para a API do Hacker News, que nos devolve as top stories. Após isto, o controlador envia as informações para o SearchModule.


# Conlcusão 

Com este projeto, conseguimos implementar um motor de busca web, semelhante ao Google, com todas as funcionalidades pedidas. Para além disso, conseguimos integrar o nosso projeto com a API do Hacker News, de modo a indexar as top stories e as stories de um utilizador. Com isto aprendemos a criar um sistema distribuido, com múltiplos componentes, que comunicam entre si. Para além disso, aprendemos a usar o springboot para criar uma interface web, bem como a usar o websocket para obter atualizações em tempo real da página de administração, juntamente com ajax.