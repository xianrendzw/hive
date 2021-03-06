/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.exec;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;

/**
 * SecureCmdDoAs - Helper class for setting parameters and env necessary for
 * being able to run child jvm as intended user.
 * Used only when kerberos security is used
 *
 */
public class SecureCmdDoAs {
  private final Path tokenPath;

  public SecureCmdDoAs(HiveConf conf) throws HiveException, IOException{
    // Get delegation token for user from filesystem and write the token along with
    // metastore tokens into a file
    String uname = UserGroupInformation.getLoginUser().getShortUserName();
    FileSystem fs = FileSystem.get(conf);
    Token<?> fsToken = fs.getDelegationToken(uname);

    File t = File.createTempFile("hive_hadoop_delegation_token", null);
    tokenPath = new Path(t.toURI());

    //write credential with token to file
    Credentials cred = new Credentials();
    cred.addToken(fsToken.getService(), fsToken);
    cred.writeTokenStorageFile(tokenPath, conf);
  }

  public void addEnv(Map<String, String> env){
    env.put(UserGroupInformation.HADOOP_TOKEN_FILE_LOCATION,
        tokenPath.toUri().getPath());
  }

}
