<template>
  <div class="bg">
    <div id="app" style="text-align: center">
      <img alt="Vue logo" src="./assets/book.png" style="width: 200px" />

      <el-container direction="vertical" class="search-and-result">
        <el-container style="text-align: center">
          <div class="search-engine-title">Book Search Engine</div>
        </el-container>
        <el-container direction="vertical" class="search-line">
          <el-row :gutter="20">
            <el-col :span="21">
              <!-- <el-input v-model="inp" placeholder="search any book"></el-input> -->
              <el-autocomplete
                class="autocomplete-display"
                v-model="inp"
                :fetch-suggestions="suggest"
                placeholder="search any book"
                :trigger-on-focus="false"
                @select="handleSelect"
              ></el-autocomplete>
            </el-col>
            <el-col :span="3">
              <el-button style="width: 100%" type="primary" @click.native="search"
                >Search</el-button
              >
            </el-col>
          </el-row>
        </el-container>

        <el-container direction="vertical">
          <el-container v-for="item in result.data" :key="item._id">
            <el-container class="result-item">
              <el-container direction="vertical">
                <el-row>
                  <el-col :span="1">
                    <el-checkbox v-model="item.checked"></el-checkbox>
                  </el-col>
                  <el-col :span="23">
                    <div style="text-align: left">
                      <a v-bind:href="'https://www.goodreads.com/book/show/' + item._id">
                        {{ item._source.title }}
                      </a>
                    </div>
                  </el-col>
                </el-row>

                <p class="result-item-summary">{{ item._source.summary }}</p>
              </el-container>
              <!-- {{ item._source.title }} -->
            </el-container>
          </el-container>
        </el-container>
      </el-container>
      <!-- HelloWorld msg="Welcome to Your Vue.js App"/ -->
    </div>
  </div>
</template>

<script>
// import HelloWorld from './components/HelloWorld.vue'
// const { Client } = require("@elastic/elasticsearch");
import axios from "axios";

// var esClient;

export default {
  name: "App",
  components: {
    // HelloWorld
  },
  data: function () {
    return {
      inp: "",
      result: {
        data: [],
      },
    };
  },
  methods: {
    search: async function () {
      var resp;
      var chosen = this.result.data.filter((item) => item.checked);
      if (chosen.length == 0) {
        resp = await axios({
          method: "post",
          url: "http://localhost:8092/query",
          data: { title: this.inp },
        });
        console.log(resp);
        this.result.data = resp.data.hits;
      } else {
        console.log(chosen);
        resp = await axios({
          method: "post",
          url: "http://localhost:8092/recommend",
          data: { titles: chosen.map((item) => item._source.title) },
        });
        console.log(resp);
        this.result.data = resp.data.hits.map((item) => ({
          _id: item.docId,
          _source: {
            title: item.title,
            summary: item.summary,
            genre: item.genre,
          },
        }));
      }
    },
    suggest: async function (queryString, cb) {
      var resp = await axios({
        method: "post",
        url: "http://localhost:8092/suggest",
        data: { title: queryString },
      });
      console.log(resp, cb);
      var options = resp.data[0].options;
      for (var i = 0; i < options.length; i++) {
        options[i].value = options[i].text;
        options[i].checked = false;
        //Do something
      }
      cb(options);
    },
    handleSelect: function () {},
  },
  mounted(item) {
    console.log(item);
  },
};
</script>

<style>
body {
  background-color: #262b35;
}

#app {
  /* bg color */
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

.search-and-result {
  width: 60%;
  /* center */
  margin: 10px auto;
}

.search-engine-title {
  /* center */
  font-size: 28px;
  color: #eeeeee;
  margin: 0 auto 20px auto;
}

.search-line {
  margin-bottom: 40px;
}

.result-item {
  background-color: #eeeeee;
  margin: 10px 0;
  border-radius: 8px;
  padding: 12px;
}

/* https://juejin.cn/post/6844903461209767944 */
.result-item-summary {
  /* override p */
  margin: 8px 0 0 0;
  text-align: left;

  position: relative;
  line-height: 1.5em;
  /* 高度为需要显示的行数*行高，比如这里我们显示两行，则为3 */
  height: 3em;
  overflow: hidden;
}

.result-item-summary:after {
  /* content: "..."; */
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 0 5px;
  background-color: #fff;
}

.result-item:hover {
  background-color: #cccccc;
}

.autocomplete-display {
  display: block;
}
</style>
