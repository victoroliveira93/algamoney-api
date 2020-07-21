package com.example.algamoney.api.resource;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Categoria;
import com.example.algamoney.api.repository.CategoriaRepository;

@RestController 
@RequestMapping("/categorias")
public class CategoriaResource {
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@GetMapping
	public List<Categoria> listar() {
		return categoriaRepository.findAll();
	}
	
	@PostMapping
	public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria, HttpServletResponse response) {
		Categoria categoriaSalva = categoriaRepository.save(categoria);

		publisher.publishEvent(new RecursoCriadoEvent(this, response, categoriaSalva.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
	}
	
	
	@GetMapping("/{codigo}")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long codigo) {
		return this.categoriaRepository.findById(codigo).
				map(categoria -> ResponseEntity.ok(categoria))
				.orElse(ResponseEntity.notFound().build());
	}

}

/*
 * A partir do Spring Boot 2.3, pra usar o @Valid precisamos adicionar explicitamente a seguinte dependência:
<dependency> 
    <groupId>org.springframework.boot</groupId> 
    <artifactId>spring-boot-starter-validation</artifactId> 
</dependency>
 * 
 * O @Valid é para validar (notnull, maxlength,...) antes de fazer o POST, para evitar o erro 500.
 * Ele pode dar o 400 Bad Request
 */

/*
 * RestController, o retorno vai ser JSON, não precisa ficar fazendo anotação extras nos métodos.
 * 
 * No get, eu posso retornar também um ReponseEntity<?> que é uma entidade, mas sem o tipo definido
 * Se a lista estiver vazia, poderia retornar ResponseEntity.noContent().build(), mas o ideal é mandar a lista vazia mesmo
 * 
 * No get de apenas uma categoria,  correto é enviar 404, pode ser feito da seguintes formas:
 * 
 * @GetMapping("/{codigo}")
public ResponseEntity<Categoria> buscarPeloCodigo(@PathVariable Long codigo) {
  return this.categoriaRepository.findById(codigo)
      .map(categoria -> ResponseEntity.ok(categoria))
      .orElse(ResponseEntity.notFound().build());
}

ou 
@GetMapping("/{codigo}")
public ResponseEntity<Categoria> buscarPeloCodigo(@PathVariable Long codigo) {
    Optional<Categoria> categoria = this.categoriaRepository.findById(codigo);
    return categoria.isPresent() ? 
            ResponseEntity.ok(categoria.get()) : ResponseEntity.notFound().build();
}
 * 
 * {No POST, para você retornar no Headers o local em que a pessoa pode
 * obter o recurso criado, você deve enviar um HttpServletResponse no método
 * POST e:
 * URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{codigo}")
			.buildAndExpand(categoriaSalva.getCodigo()).toUri();
 * 
 * ServletUriComponentsBuilder -> Cria Uri
 * fromCurrentResquestUri -> /categorias do localhost, DEV, HOM, PROD,...
 * path -> adiciona /{codigo}
 * .buildAndExpand(categoriaSalva.getCodigo()).toUri() -> adiciona o codigo na URI
 * Para monsta a URI de onde está o getCodigo e:
 * 	response.setHeader("Location", uri.toASCIIString());
 *
 * Para colocar o Location nos headers da resposta
 * 
 * Você pode colocar a anotação @ResponseStatus(HttpStatus.CREATED) no método para retornar o código 201,
 * mas isso se torna desnecessário se você enviar:
 * return ResponseEntity.created(uri).body(categoriaSalva),
 * pois ele já envia o 201 e envia no body a categoria criada,
 * mesmo sem ter o método getCategoria criado ainda.
 * O método created(uri) já seta o header Location, então response.setHeader("Location", uri.toASCIIString());
 * deve ser apagado para que o Location não venha duplicado no Headers
 * }
 * */

/*
 * Pq retornar ResponseEntity<Categoria> em vez de Categoria?
 * Se for, o ResponseEntity encapsula uma série de outras coisas que estão relacionadas diretamente a resposta HTTP.

Por exemplo, se retornássemos somente a categoria, estaríamos falando que nossa resposta seria apenas a representação do objeto categoria, e o restante seria retornado com valores padrão.

Então, o status HTTP seria 200, nenhum Header poderia ser adicionado (como o caso do location).

Utilizando essa classe, temos uma flexibilidade maior para manipular os dados da resposta HTTP. Então (como foi feito na aula), podemos mandar um status code diferente de 200, podemos adicionar headers na resposta, definir o corpo da resposta, enfim, manipulamos a resposta completa.
 * */

/*
 * findOne() vs findById()?
 * findOne você tem q criar uma categoria e setar o codigo nela,
 * criar um exemplo dessa categoria criada e enviar ele como parâmetro: 
 * Categoria categoriaExample = new Categoria();
    categoriaExample.setCodigo(codigo);
    
    Example<Categoria> example = Example.of(categoriaExample);
    
    return this.categoriaRepository.findOne(example).orElse(null);
    
    já o findById() é só mandar o codigo:
    return this.categoriaRepository.findById(codigo).orElse(null);
    
    o orElse(null) é necessário pq findById retorna um Optional e o método
    está retornando uma Categoria
 * 
 */

/*
@PathVariable vs @RequestParam
PathVariable é direto na URL: localhost:8080/categorias/1
RequestParam é na URL, após ?: localhost:8080/categorias?codigo=1
*/

/*
ServletUriComponentsBuilder: Possui métodos para criar links com base no HttpServletRequest atual (ou seja, na requisição atual).

Basicamente o que fazemos ali é pegar a URI "localhost:8080/categorias"
 que foi a URI da requisição e adicionamos o código da nova categoria 
 criada, ficando "localhost:8080/categorias/1", por exemplo. 
 Fazemos isso pois em outros servidores e ambientes como de produção, 
 o nome do host não vai ser "localhost". Isso vai ser dinâmico de acordo 
 com o host.
*/

/*
@PostMapping
public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria) {
	Categoria categoriaSalva = categoriaRepository.save(categoria);
	URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{codigo}")
		.buildAndExpand(categoriaSalva.getCodigo()).toUri();
	
	return ResponseEntity.created(uri).body(categoriaSalva);
}

O código do metodo criar era assim, mas como essa parte da URI será repetida em outras classes,
foi criado o RecursoCriadoEvent que é um ApplicationEvent e um RecursoCriadoListener que é
um ApplicationListener<RecursoCriadoEvent>

Injetamos um Publicador de Evento de aplicação
@Autowired
	private ApplicationEventPublisher publisher;
	
e fazemos ele publicar o evento:
publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoaSalva.getCodigo()));
this -> A classe q publicou o evento, ou seja, essa classe mesmo

O método fica então assim:
	@PostMapping
	public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria, HttpServletResponse response) {
		Categoria categoriaSalva = categoriaRepository.save(categoria);

		publisher.publishEvent(new RecursoCriadoEvent(this, response, categoriaSalva.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
	}
	
Foi necessário inserir o HttpServletResponse na assinatura do método e mudar o return,
substituindo o .created(uri) por .status(HttpStatus.CREATED)
por que não tem a variável uri mais no método
*/

/*
No método Delete é retornado void, mas tem o @ResponseStatus(HttpStatus.NO_CONTENT)
para pelo menos retornar o 204 (no content) que significa
que o método deu certo, mas não tem conteúdo pra retornar

Lembre-se:
faixa 200: Ok
faixa 400: erros
*/