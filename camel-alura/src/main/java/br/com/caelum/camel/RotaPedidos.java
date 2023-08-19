package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
//				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s").
//					to("http4://argentumws-spring.herokuapp.com/negociacoes").
//						convertBodyTo(String.class).
//						log("${body}").
//						setHeader(Exchange.FILE_NAME, constant("negociacoes.xml")).
//				to("file:saida");
				
				from("file:pedidos?delay=5s&noop=true").
					setProperty("pedidoId", xpath("/pedido/id/text()")).
					setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()")).
					split().
						xpath("/pedido/itens/item").
					filter().
						xpath("/item/formato[text()='EBOOK']").
						setProperty("ebookId", xpath("/item/livro/codigo/text()")).
					log("${id}").
					marshal().
						xmljson().
					log("${body}").
					setHeader(Exchange.HTTP_METHOD, HttpMethods.GET).
					setHeader(Exchange.HTTP_QUERY, simple("clienteId=${property.clienteId}&pedidoId=${property.pedidoId}&ebookId=${property.ebookId}")).
				to("http4://localhost:8080/webservices/ebook/item");
				
			}		
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();

	}	
}
