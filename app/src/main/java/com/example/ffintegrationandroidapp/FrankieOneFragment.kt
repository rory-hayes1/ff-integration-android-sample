package com.example.ffintegrationandroidapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ffintegrationandroidapp.databinding.FragmentFirstBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FrankieOneFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    private var _binding: FragmentFirstBinding? = null

    private var uri: Uri? = null
    private var takeDocumentPicture: ActivityResultLauncher<Uri>? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        binding.webView.apply {
            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            addJavascriptInterface(WebAppInterface(requireContext()), "Android")
            loadUrl("https://visionary-donut-d00f62.netlify.app/")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.v(TAG, "OnBack Pressed")
                handleWebViewNavigation()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        takeDocumentPicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    // The image was saved into the given Uri -> do something with it
                    val msg = "Image captured successfully at : $uri"
                    Log.v(TAG, msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    filePathCallback?.onReceiveValue(arrayOf(uri!!))
                }
            }
    }

    fun handleWebViewNavigation() {
        Log.v(TAG, "Handling Webview navigation after back pressed action")
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            Log.v(TAG, "Navigating Up from Screen")
            if (!findNavController().popBackStack()) {
                // Call finish() on your Activity
                requireActivity().finish()
            }
        }
    }

    /** Instantiate the interface and set the context  */
    private inner class WebAppInterface(private val mContext: Context) {

        /** Show a toast from the web page  */
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Log.v(TAG, "Url loading is $url")
            return false
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {

        override fun onPermissionRequest(request: PermissionRequest?) {
            Log.i(TAG, "onPermissionRequest ${request?.resources}")
            val requestedResources = request!!.resources
            for (r in requestedResources) {
                if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                    request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                    break
                }
            }
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest?) {
            super.onPermissionRequestCanceled(request)
            Log.i(TAG, "onPermissionRequestCanceled ${request?.resources}")
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallbackParam: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Log.v(TAG, "Show a file chooser")
            filePathCallback = filePathCallbackParam

            uri = createImageFile()?.let {
                FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.ffintegrationandroidapp.fileprovider",
                    it
                )
            }
            takeDocumentPicture?.launch(uri);

            return true
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SS",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        return image
    }

}