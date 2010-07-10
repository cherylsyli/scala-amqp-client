package iconara.amqp


import com.rabbitmq.client.{
  Channel => RMQChannel, 
  MessageProperties
}


class Exchange(val name: String, channel: RMQChannel) {
  def publish(routingKey: String, message: String, mandatory: Boolean = false, immediate: Boolean = false) {
    channel.basicPublish(name, routingKey, mandatory, immediate, MessageProperties.TEXT_PLAIN, message.getBytes())
  }
  
  def bind(routingKey: String, queues: Queue *) {
    queues.foreach(_.bind(this, routingKey))
  }
  
  def delete() {
    channel.exchangeDelete(name)
  }
}