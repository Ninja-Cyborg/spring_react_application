package com.backend.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    private S3Service underTest;

    @BeforeEach
    void setUp() {
        underTest = new S3Service(s3Client);
    }

    @Test
    void canPutObject() throws IOException {
        // GIVEN
        String bucket = "member";
        String key = "foo/bar";
        byte[] file = "Test file".getBytes();

        // WHEN
        underTest.putObject(bucket, key, file);

        // THEN
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        ArgumentCaptor<RequestBody> requestBodyArgumentCaptor =
                ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(
                putObjectRequestArgumentCaptor.capture(),
                requestBodyArgumentCaptor.capture()
        );

        PutObjectRequest putObjectRequestArgumentCaptorValue =
                putObjectRequestArgumentCaptor.getValue();

        assertThat(putObjectRequestArgumentCaptorValue.bucket()).isEqualTo(bucket);
        assertThat(putObjectRequestArgumentCaptorValue.key()).isEqualTo(key);

        RequestBody requestBodyArgumentCaptorValue = requestBodyArgumentCaptor.getValue();

        assertThat(requestBodyArgumentCaptorValue
                .contentStreamProvider()
                .newStream()
                .readAllBytes()
        ).isEqualTo(RequestBody.fromBytes(file)
                        .contentStreamProvider()
                        .newStream()
                        .readAllBytes()
                );
    }

    @Test
    void canGetObject() throws IOException {
        // GIVEN
        String bucket = "member";
        String key = "foo/bar";
        byte[] file = "Test file".getBytes();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> res = mock(ResponseInputStream.class);

        when(res.readAllBytes()).thenReturn(file);
        when(s3Client.getObject(eq(getObjectRequest))).thenReturn(res);

        // WHEN
        byte[] bytes = underTest.getObject(bucket, key);

        // THEN
        assertThat(bytes).isEqualTo(file);
    }

    @Test
    void willThrowWhenGetObject() throws IOException{
        // GIVEN
        String bucket = "member";
        String key = "foo/bar";
        byte[] file = "Test file".getBytes();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> res = mock(ResponseInputStream.class);

        when(res.readAllBytes()).thenThrow(new IOException("Cannot read file data"));
        when(s3Client.getObject(eq(getObjectRequest))).thenReturn(res);

        // WHEN

        // THEN
        assertThatThrownBy(() -> underTest.getObject(bucket, key))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot read file data")
                .hasRootCauseInstanceOf(IOException.class);

    }
}