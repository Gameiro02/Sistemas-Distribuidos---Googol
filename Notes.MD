Arquitetura dos storage barrels:
    - 2 barrels for each thread
    - Barrel one: word;link1;link2;
    - Barrel two: link;title_of_the_page;context;link_pointing1;link_pointing2;

data = [link, title, context]