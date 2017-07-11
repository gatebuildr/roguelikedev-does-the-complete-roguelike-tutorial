package input

import kotlinx.coroutines.experimental.channels.Channel

interface InputSource {
    val inputChannel: Channel<Input>
}