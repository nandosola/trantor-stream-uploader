/*
 * Copyright abstra.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.abstra.trantor.wcamp;


import java.util.ArrayList;
import java.util.List;

public class CustomHttpHeaders {

    private CustomHttpHeaders() {}

    /** Set by the API client */
    public static final String X_TRANTOR_CLIENT_ID = "X-Trantor-Client-Id";
    public static final String X_TRANTOR_ASSIGNED_UPLOAD_ID = "X-Trantor-Assigned-Upload-Id";

    /** Set by FileServer */
    public static final String X_TRANTOR_UPLOADED_FILES_INFO = "X-Trantor-Uploaded-Files-Info";
    //example: X-Trantor-Uploaded-Files-Info: deadbeef/Brian's Life; coffeebabe/img_009; ...

    /** Set by the Wcamp client
     * Can be either blank or 'version'
     */
    public static final String X_TRANTOR_UPLOAD_TYPE = "X-Trantor-Upload-Type";
    public static final String X_TRANTOR_UPLOAD_NAME = "X-Trantor-Upload-Name";
    public static final String X_TRANTOR_DOCUMENT_ID = "X-Trantor-Document-Id";
    public static final String X_TRANTOR_ERROR_NONCE = "X-Trantor-Error-Nonce";

    public static final List<String> validUploadTypesLc;
    static {
        validUploadTypesLc = new ArrayList<String>() {{
            add("version");
        }};
    }

    public static final List<String> doNotCopyLc;
    static {
        doNotCopyLc = new ArrayList<String>() {{
            add(X_TRANTOR_CLIENT_ID.toLowerCase());
            add(X_TRANTOR_ASSIGNED_UPLOAD_ID.toLowerCase());
            add(X_TRANTOR_UPLOADED_FILES_INFO.toLowerCase());
            add(X_TRANTOR_UPLOAD_TYPE.toLowerCase());
            add(X_TRANTOR_DOCUMENT_ID.toLowerCase());
        }};
    }
}
