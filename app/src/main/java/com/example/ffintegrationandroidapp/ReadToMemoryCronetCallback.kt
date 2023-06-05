package com.example.ffintegrationandroidapp

import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

abstract class ReadToMemoryCronetCallback : UrlRequest.Callback() {

    private val bytesReceived = ByteArrayOutputStream()
    private val receiveChannel = Channels.newChannel(bytesReceived)
    private val BYTE_BUFFER_CAPACITY_BYTES: Int = 16384

    override fun onRedirectReceived(
        request: UrlRequest,
        info: UrlResponseInfo,
        newLocationUrl: String?
    ) {
        request.followRedirect()
    }

    override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
        val bodyBytes = bytesReceived.toByteArray()
        onSucceeded(request, info, bodyBytes);
    }

    override fun onFailed(request: UrlRequest, info: UrlResponseInfo?, error: CronetException) {
        TODO("Not yet implemented")
    }

    override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES))
    }

    override fun onReadCompleted(
        request: UrlRequest,
        info: UrlResponseInfo,
        byteBuffer: ByteBuffer
    ) {
        // The byte buffer we're getting in the callback hasn't been flipped for reading,
        // so flip it so we can read the content.
        byteBuffer.flip()
        receiveChannel.write(byteBuffer)

        // Reset the buffer to prepare it for the next read
        byteBuffer.clear()

        // Continue reading the request
        request.read(byteBuffer)
    }

    abstract fun onSucceeded(
        request: UrlRequest, info: UrlResponseInfo, bodyBytes: ByteArray)
}