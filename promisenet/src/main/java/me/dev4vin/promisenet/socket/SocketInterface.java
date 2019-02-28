/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.dev4vin.promisenet.socket;


import me.dev4vin.promisemodel.List;

public class SocketInterface {
  private String event;
  private List<Object> args;
  private FastParserSocketIO fastParserSocketIO;

  public String event() {
    return event;
  }

  public FastParserSocketIO fastParserSocketIO() {
    return fastParserSocketIO;
  }

  protected SocketInterface fastParserSocketIO(FastParserSocketIO fastParserSocketIO) {
    this.fastParserSocketIO = fastParserSocketIO;
    return this;
  }

  public SocketInterface event(String event) {
    this.event = event;
    return this;
  }

  public List<Object> args() {
    return args;
  }

  public SocketInterface args(List<Object> args) {
    this.args = args;
    return this;
  }
}
