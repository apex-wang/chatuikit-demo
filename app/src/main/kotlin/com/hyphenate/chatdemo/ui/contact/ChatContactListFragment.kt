package com.hyphenate.chatdemo.ui.contact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.viewmodel.ChatContactViewModel
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.feature.contact.EaseContactsListFragment
import com.hyphenate.easeui.model.EaseUser
import com.hyphenate.easeui.viewmodel.contacts.IContactListRequest

class ChatContactListFragment : EaseContactsListFragment() {

    private var contactViewModel: IContactListRequest? = null
    companion object{
        private val TAG = ChatContactListFragment::class.java.simpleName
    }

    override fun initView(savedInstanceState: Bundle?) {
        contactViewModel = ViewModelProvider(context as AppCompatActivity)[ChatContactViewModel::class.java]
        binding?.listContact?.setViewModel(contactViewModel)
        super.initView(savedInstanceState)
    }

    override fun loadContactListSuccess(userList: MutableList<EaseUser>) {
        super.loadContactListSuccess(userList)
        binding?.listContact?.let {
            (it.rvContactList.layoutManager as? LinearLayoutManager)?.let { manager->
                it.post {
                    val firstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
                    val visibleList = it.getListAdapter()?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    val fetchList = visibleList?.filter { user ->
                        val u = DemoHelper.getInstance().getDataModel().getUser(user.userId)
                        (u == null || u.updateTimes == 0) &&
                                (user.nickname.isNullOrEmpty() || user.avatar.isNullOrEmpty())
                    }
                    fetchList?.let {
                        contactViewModel?.fetchContactInfo(fetchList)
                    }
                }
            }
        }
    }

    override fun loadContactListFail(code: Int, error: String) {
        super.loadContactListFail(code, error)
        ChatLog.e(TAG,"loadContactListFail demo $code $error")
    }

    class Builder:EaseContactsListFragment.Builder() {
        override fun build(): EaseContactsListFragment {
            if (customFragment == null) {
                customFragment = ChatContactListFragment()
            }
            if (customFragment is ChatContactListFragment){

            }
            return super.build()
        }
    }

}