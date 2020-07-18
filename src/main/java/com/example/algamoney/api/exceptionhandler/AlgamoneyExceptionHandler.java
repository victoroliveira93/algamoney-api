package com.example.algamoney.api.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
	    HttpHeaders headers, HttpStatus status, WebRequest request) {

	  String mensagemUsuario = this.messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
	  String mensagemDesenvolvedor = Optional.ofNullable(ex.getCause()).orElse(ex).toString();

	  List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));

	  return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		List<Erro> erros = criarListaDeErros(ex.getBindingResult());
		return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@ExceptionHandler({EmptyResultDataAccessException.class})
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, 
			WebRequest request) {
		String mensagemUsuario = messageSource.getMessage("recurso.nao-encontrado", null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.toString();//Aqui não dá getCause, pois já está recebendo a causa direto
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
		String mensagemUsuario = messageSource.getMessage("recurso.operacao-nao-permitida", null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex); //necessário a dependencia descrita abaixo (lang3)
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	private List<Erro> criarListaDeErros(BindingResult bindingResult) {
		List<Erro> erros = new ArrayList<>();
		
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String mensagemUsuario = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			String mensagemDesenvolvedor = fieldError.toString();
			erros.add(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		}
		return erros;
	}
	
	public static class Erro {
		
		private String mensagemUsuario;
		private String mensagemDesenvolvedor;
		
		public Erro(String mensagemUsuario, String mensagemDesenvovedor) {
			super();
			this.mensagemUsuario = mensagemUsuario;
			this.mensagemDesenvolvedor = mensagemDesenvovedor;
		}

		public String getMensagemUsuario() {
			return mensagemUsuario;
		}

		public String getMensagemDesenvolvedor() {
			return mensagemDesenvolvedor;
		}
	}
}

/*
 * <dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.4</version>
	</dependency>
	
	Dependencia usada para poder dar a causa específica do erro.
	Ex.: Um campo foi com um valor inexistente.
 */

/*
 * Você mesmo pode criar aqui uma classe para lidar com alguma exceção em específico,
 * como exemplo a 	
 * @ExceptionHandler({EmptyResultDataAccessException.class})
 * @ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleEmptyResultDataAccessException() {
		
	}
	
	Ela sem implementação nenhuma já mandaria um 404 para o cliente
 * 
 */

/*
Para poder mandar exceções em todo o sistema, essa classe deve ser um Controller do tipo
Advice, para poder observar toda a aplicação

handleHttpMessageNotReadable lida com mensagem não lida, por exemplo, 
quando manda dados a mais na requisição e está configurando para
lançar erro.

Você pode criar um arquivo de mensagens como o nome exatamente: messages.properties
na pasta resources.
Modelo do arquivo (á tem q ser em UNICODE):
mensagem.invalida="Mensagem inv\u00E1lida

O MessageSource que já vem no Spring pegará a mensagem:
String mensagemUsuario = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
O arquivo de mensagens está no resources/messages.properties
O arquivo de mensagens de validação está no resources/ValidationsMessages.properties, você pode colocar
o nome do campo: categoria.nome = Nome no arquivo messages.properties.
No ValidationsMessages você coloca {0} pra ser o nome do argumento, se ele não tiver o nome no messages.properties
ele aparecerá com o nome da variável
O nome do campo no ValidationMessages é retirado da interface @NotNull, @Size,...
String message() default "{javax.validation.constraints.Size.message}";

LocaleContextHolder.getLocale() -> pega o Locale atual

Pq Erro é classe static?
Por convenção utilizamos static em inner classes, para que seu uso em outros contextos seja independente da classe eterna.

Se precisarmos utilizar a classe Erro fora da classe AlgamoneyExceptionHandler, sem o modificador "static", precisaríamos antes ter uma instância de AlgamoneyExceptionHandler.

BindingResult tem a lista de todos os erros, FieldErros são os erros dos campos
*/