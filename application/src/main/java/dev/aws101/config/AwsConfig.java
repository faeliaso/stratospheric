package dev.aws101.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AwsConfig {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${custom.aws.local.endpoint:#{null}}")
  private String endpoint;

  @Bean
  @ConditionalOnProperty(value = "custom.security.enabled", havingValue = "true", matchIfMissing = true)
  public AWSCognitoIdentityProvider awsCognitoIdentityProvider(AWSCredentialsProvider awsCredentialsProvider) {
    return AWSCognitoIdentityProviderAsyncClientBuilder.standard()
      .withCredentials(awsCredentialsProvider)
      .withRegion(region)
      .build();
  }

  @Bean
  public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync) {
    return new QueueMessagingTemplate(amazonSQSAsync);
  }

  @Bean
  public NotificationMessagingTemplate notificationMessagingTemplate(AmazonSNS amazonSNS) {
    return new NotificationMessagingTemplate(amazonSNS);
  }

  @Bean(name = "amazonSNS")
  public AmazonSNS amazonSNS(AWSCredentialsProvider awsCredentialsProvider) {
    AwsClientBuilder.EndpointConfiguration endpointConfiguration = null;
    if (endpoint != null) {
      endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
    }

    AmazonSNSClientBuilder amazonSNSClientBuilder = AmazonSNSClientBuilder.standard();
    if (endpointConfiguration != null) {
      amazonSNSClientBuilder = amazonSNSClientBuilder.withEndpointConfiguration(endpointConfiguration);
    } else {
      amazonSNSClientBuilder = amazonSNSClientBuilder
        .withRegion(region)
        .withCredentials(awsCredentialsProvider);
    }

    return amazonSNSClientBuilder.build();
  }
}
