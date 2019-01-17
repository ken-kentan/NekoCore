package jp.kentan.minecraft.nekocore.data.dao

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

class PlayerDao(
    private val database: NekoCoreDatabase
) : BaseDao() {

    private val adapter: JsonAdapter<List<String>> by lazy {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        Moshi.Builder().build().adapter<List<String>>(type)
    }

    fun insertDefaultRecordIfNeeded(uuid: UUID): Unit =
        database.connection.use { conn ->
            conn.prepareStatement("INSERT IGNORE INTO neko_player (id) VALUES (?)").apply {
                setString(1, uuid.toString())
                execute()
                close()
            }
        }

    fun getVoteData(uuid: UUID): Pair<Int, Date?> =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT vote_continuous, last_vote_date FROM neko_player WHERE id = ?")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val data = if (result.next()) {
                Pair(result.getInt(1), result.getTimestamp(2))
            } else {
                Pair(0, null)
            }

            st.close()
            return@use data
        }

    fun isLastVoteDateNull(uuid: UUID): Boolean =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT last_vote_date FROM neko_player WHERE id = ?")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val data = if (result.next()) {
                result.getTimestamp(1)
            } else {
                null
            }

            st.close()
            return@use data == null
        }

    fun updateVoteData(uuid: UUID, continuous: Int) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("UPDATE neko_player SET vote_continuous = ?, last_vote_date = CURRENT_TIMESTAMP WHERE id = ?")
            st.setInt(1, continuous)
            st.setString(2, uuid.toString())

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun getPendingCommandList(uuid: UUID): List<String> =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT pending_commands FROM neko_player WHERE id = ?")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val list = if (result.next()) {
                adapter.fromJson(result.getString(1)) ?: emptyList()
            } else {
                emptyList()
            }

            st.close()
            return@use list
        }

    fun addPendingCommandList(uuid: UUID, commandList: List<String>) {
        if (commandList.isEmpty()) {
            return
        }

        database.connection.use { conn ->
            val pendingCommandList = mutableListOf<String>()

            conn.prepareStatement("SELECT pending_commands FROM neko_player WHERE id = ?").apply {
                setString(1, uuid.toString())

                val result = executeQuery()
                if (result.next()) {
                    pendingCommandList.addAll(adapter.fromJson(result.getString(1)) ?: return@apply)
                }

                close()
            }

            pendingCommandList.addAll(commandList)

            val st =
                conn.prepareStatement("UPDATE neko_player SET pending_commands = ?, updated_date = CURRENT_TIMESTAMP WHERE id = ?")
            st.setString(1, adapter.toJson(pendingCommandList))
            st.setString(2, uuid.toString())

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }
    }

    fun deletePendingCommandList(uuid: UUID) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("UPDATE neko_player SET pending_commands = '[]', updated_date = CURRENT_TIMESTAMP WHERE id = ?")
            st.setString(1, uuid.toString())

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun getAdvertiseFrequency(uuid: UUID) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT advertise_freq FROM neko_player WHERE id = ?")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val freq = if (result.next()) result.getString(1) else null

            st.close()
            return@use freq
        }

    fun updateAdvertiseFrequency(uuid: UUID, freqName: String) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("UPDATE neko_player SET advertise_freq = ?, updated_date = CURRENT_TIMESTAMP WHERE id = ?")
            st.setString(1, freqName)
            st.setString(2, uuid.toString())

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }
}